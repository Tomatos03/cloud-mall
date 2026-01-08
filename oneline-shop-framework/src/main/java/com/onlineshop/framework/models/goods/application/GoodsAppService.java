package com.onlineshop.framework.models.goods.application;

import cn.hutool.core.collection.CollectionUtil;
import com.onlineshop.framework.enums.BizErrorCode;
import com.onlineshop.framework.exception.BusinessException;
import com.onlineshop.framework.models.goods.sku.GoodsSku;
import com.onlineshop.framework.models.goods.sku.IGoodsSkuService;
import com.onlineshop.framework.models.goods.spec.entity.GoodsSkuSpec;
import com.onlineshop.framework.models.goods.spec.entity.Spec;
import com.onlineshop.framework.models.goods.spec.entity.SpecValue;
import com.onlineshop.framework.models.goods.spec.service.IGoodsSkuSpecService;
import com.onlineshop.framework.models.goods.spec.service.ISpecService;
import com.onlineshop.framework.models.goods.spec.service.ISpecValueService;
import com.onlineshop.framework.models.goods.spu.Goods;
import com.onlineshop.framework.models.goods.spu.IGoodsService;
import com.onlineshop.framework.models.goods.spu.dto.GoodsPublishPayload;
import com.onlineshop.framework.models.goods.spu.vo.GoodsItemVO;
import com.onlineshop.framework.utils.MoneyUtil;
import com.onlineshop.framework.utils.context.UserContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品发布聚合应用服务
 * 用于处理商品发布、更新、查询等涉及多个子模块的复杂业务
 *
 * @author Tomatos
 * @date 2026/1/6
 */
@Service
public class GoodsAppService implements IGoodsAppService {

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private IGoodsSkuService skuService;

    @Autowired
    private ISpecService specService;

    @Autowired
    private ISpecValueService specValueService;

    @Autowired
    private IGoodsSkuSpecService skuSpecService;

    /**
     * 新增商品
     * 参数验证通过 @Valid 和 JSR 380 注解自动处理
     *
     * @param payload 商品发布请求对象
     */
    @Transactional(rollbackFor = Exception.class)
    public void publishGoods(GoodsPublishPayload payload) {
        Goods goods = buildGoods(payload);
        saveGoods(goods);
        saveSpecificationsAndSkus(goods.getId(), payload);
    }

    /**
     * 删除商品（包括SPU、SKU、规格关联及未被使用的规格值）
     * 删除流程：
     * 1. 校验商品存在性
     * 2. 查询商品下所有SKU及其规格值
     * 3. 删除SKU与规格的关联记录
     * 4. 删除SKU记录
     * 5. 删除SPU记录
     * 6. 清理不再被任何SKU引用的规格值（因为一个规格值可能被多个商品共享）
     *
     * @param id 商品ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteGoods(Long id) {
        Goods goods = goodsService.getById(id);
        checkGoods(goods);
        checkMerchantOwnership(goods);

        // 删除SKU及其关联规格值，并收集需要清理的规格值ID
        Set<Long> specValueIds = deleteSkusAndCollectSpecValues(id);
        // 删除SPU记录
        deleteSpu(id);

        // 清理不再被引用的规格值
        if (!CollectionUtil.isEmpty(specValueIds)) {
            deleteSpecValueIfUnused(new ArrayList<>(specValueIds));
        }
    }

    /**
     * 更新商品
     *
     * @param payload 商品发布请求对象（包含id）
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateGoods(GoodsPublishPayload payload) {
        Long goodsId = payload.getGoodsId();
        checkGoodsId(goodsId);

        // 查询原商品信息
        Goods existingGoods = goodsService.getById(goodsId);
        checkGoods(existingGoods);

        // 更新商品基本信息
        updateGoods(buildGoods(payload));

        // 删除原有的SKU和规格关联
        skuService.removeByGoodsId(goodsId);

        // 重新保存规格和SKU
        saveSpecificationsAndSkus(goodsId, payload);
    }

    private void checkGoodsId(Long goodsId) {
        if (goodsId == null) {
            throw new BusinessException(BizErrorCode.GOODS_ID_INVALID);
        }
    }

    private void updateGoods(Goods goods) {
        boolean goodsUpdated = goodsService.updateGoods(goods);
        if (!goodsUpdated) {
            throw new BusinessException(BizErrorCode.GOODS_UPDATE_FAILED);
        }
    }

    /**
     * 获取商品详情（编辑模式）
     *
     * @param id 商品ID
     * @return 商品详情
     */
    public GoodsItemVO getGoodsItem(Long id) {
        Goods goods = goodsService.getById(id);
        checkGoods(goods);

        GoodsItemVO vo = new GoodsItemVO();
        vo.setId(goods.getId());
        vo.setName(goods.getName());
        vo.setCategoryId(String.valueOf(goods.getCategoryId()));
        vo.setUnit(goods.getUnit());
        vo.setInfo(goods.getInfo());
        vo.setImg(goods.getImg());
        vo.setImgList(goods.getImgList());
        vo.setDetailImages(goods.getDescription());
        vo.setStoreId(String.valueOf(goods.getStoreId()));
        vo.setStoreName(goods.getStoreName());
        vo.setStatus(goods.getStatus() ? 1 : 0);

        // 获取规格和SKU信息
        vo.setSpecifications(getSpecificationsForGoods(id));
        vo.setSkus(getSkusForGoods(id));

        return vo;
    }

    /**
     * 获取商品的规格信息
     *
     * @param goodsId 商品ID
     * @return 规格列表
     */
    private List<GoodsItemVO.GoodsSpecification> getSpecificationsForGoods(Long goodsId) {
        List<GoodsSku> skus = skuService.listByGoodsId(goodsId);
        if (skus.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, GoodsItemVO.GoodsSpecification> specMap = new LinkedHashMap<>();

        for (GoodsSku sku : skus) {
            List<GoodsSkuSpec> skuSpecs = skuSpecService.listBySkuId(sku.getId());
            for (GoodsSkuSpec skuSpec : skuSpecs) {
                if (!specMap.containsKey(skuSpec.getSpecId())) {
                    Spec spec = specService.getSpecById(skuSpec.getSpecId());
                    if (spec != null) {
                        GoodsItemVO.GoodsSpecification specification =
                                new GoodsItemVO.GoodsSpecification();
                        specification.setSpecId(spec.getId());
                        specification.setName(spec.getName());
                        specification.setValues(new ArrayList<>());
                        specMap.put(spec.getId(), specification);
                    }
                }
            }
        }

        // 填充规格值
        for (GoodsSku sku : skus) {
            List<GoodsSkuSpec> skuSpecs = skuSpecService.listBySkuId(sku.getId());
            for (GoodsSkuSpec skuSpec : skuSpecs) {
                GoodsItemVO.GoodsSpecification specification = specMap.get(skuSpec.getSpecId());
                if (specification != null) {
                    SpecValue specValue = specValueService.getValueById(
                            skuSpec.getSpecValueId());
                    if (specValue != null) {
                        boolean exists = specification.getValues()
                                                      .stream()
                                                      .anyMatch(v -> v.getSpecValueId()
                                                                      .equals(specValue.getId()));
                        if (!exists) {
                            GoodsItemVO.GoodsSpecValue value = new GoodsItemVO.GoodsSpecValue();
                            value.setSpecValueId(specValue.getId());
                            value.setValue(specValue.getValue());
                            specification.getValues()
                                         .add(value);
                        }
                    }
                }
            }
        }

        return new ArrayList<>(specMap.values());
    }

    /**
     * 获取商品的SKU信息
     *
     * @param goodsId 商品ID
     * @return SKU列表
     */
    private List<GoodsItemVO.GoodsSkuItem> getSkusForGoods(Long goodsId) {
        List<GoodsSku> skus = skuService.listByGoodsId(goodsId);
        return skus.stream()
                   .map(sku -> {
                       GoodsItemVO.GoodsSkuItem skuItem = new GoodsItemVO.GoodsSkuItem();
                       skuItem.setSkuId(sku.getId());
                       skuItem.setPrice(sku.getPrice());
                       skuItem.setInventory(sku.getInventory());
                       skuItem.setStatus(sku.getStatus());

                       // 获取SKU的规格信息
                       List<GoodsSkuSpec> skuSpecs = skuSpecService.listBySkuId(sku.getId());
                       List<GoodsItemVO.SkuSpec> specs = new ArrayList<>();
                       for (GoodsSkuSpec skuSpec : skuSpecs) {
                           Spec spec = specService.getSpecById(skuSpec.getSpecId());
                           SpecValue specValue = specValueService.getValueById(
                                   skuSpec.getSpecValueId());
                           if (spec != null && specValue != null) {
                               GoodsItemVO.SkuSpec specItem = new GoodsItemVO.SkuSpec();
                               specItem.setSpecId(spec.getId());
                               specItem.setName(spec.getName());
                               specItem.setSpecValueId(specValue.getId());
                               specItem.setValue(specValue.getValue());
                               specs.add(specItem);
                           }
                       }
                       skuItem.setSpecs(specs);

                       return skuItem;
                   })
                   .collect(Collectors.toList());
    }

    private void checkGoods(Goods goods) {
        if (goods == null) {
            throw new BusinessException(BizErrorCode.GOODS_OR_SHOP_NOT_EXIST);
        }
    }

    private void checkMerchantOwnership(Goods goods) {
        Long currentStoreId = UserContextHolder.getStoreId();
        if (!Objects.equals(currentStoreId, goods.getStoreId())) {
            throw new BusinessException(BizErrorCode.NO_PERMISSION);
        }
    }

    /**
     * 删除商品的所有SKU及其关联数据，并收集需要清理的规格值ID
     *
     * @param goodsId 商品ID
     * @return 需要清理的规格值ID集合
     */
    private Set<Long> deleteSkusAndCollectSpecValues(Long goodsId) {
        List<GoodsSku> skus = skuService.listByGoodsId(goodsId);
        List<Long> skuIds = skus.stream()
                                .map(GoodsSku::getId)
                                .collect(Collectors.toList());

        Set<Long> specValueIds = new HashSet<>();
        if (!CollectionUtil.isEmpty(skuIds)) {
            // 收集所有规格值ID
            specValueIds = collectSpecValueIdsFromSkus(skuIds);

            // 删除SKU与规格的关联记录
            skuSpecService.removeBySkuIds(skuIds);

            // 删除SKU记录
            skuService.removeByGoodsId(goodsId);
        }
        return specValueIds;
    }

    /**
     * 删除商品SPU记录
     *
     * @param id 商品ID
     */
    private void deleteSpu(Long id) {
        if (!goodsService.removeById(id)) {
            throw new BusinessException(BizErrorCode.SPU_DELETE_FAILED);
        }
    }

    /**
     * 删除不再被任何SKU引用的规格值
     * 注意：一个规格值可能被多个商品共享，只有当没有任何SKU引用时才能删除
     *
     * @param specValueIds 规格值ID列表
     */
    private void deleteSpecValueIfUnused(List<Long> specValueIds) {
        if (CollectionUtil.isEmpty(specValueIds)) {
            return;
        }

        for (Long specValueId : specValueIds) {
            long refCount = skuSpecService.countBySpecValueId(specValueId);
            if (refCount == 0) {
                boolean removed = specValueService.removeById(specValueId);
                if (!removed) {
                    throw new BusinessException(BizErrorCode.SPEC_VALUE_DELETE_FAILED);
                }
            }
        }
    }

    /**
     * 从SKU列表中收集所有关联的规格值ID
     *
     * @param skuIds SKU ID列表
     * @return 规格值ID集合
     */
    private Set<Long> collectSpecValueIdsFromSkus(List<Long> skuIds) {
        Set<Long> specValueIds = new HashSet<>();
        for (Long skuId : skuIds) {
            List<GoodsSkuSpec> skuSpecs = skuSpecService.listBySkuId(skuId);
            for (GoodsSkuSpec skuSpec : skuSpecs) {
                specValueIds.add(skuSpec.getSpecValueId());
            }
        }
        return specValueIds;
    }

    private Goods buildGoods(GoodsPublishPayload payload) {
        return Goods.builder()
                    .id(payload.getGoodsId())
                    .name(payload.getName())
                    .categoryId(payload.getCategoryId())
                    .unit(payload.getUnit())
                    .info(payload.getInfo())
                    .img(payload.getImg())
                    .imgList(payload.getImgList())
                    .description(payload.getDetailImages())
                    .storeId(Long.parseLong(payload.getStoreId()))
                    .storeName(payload.getStoreName())
                    .status(payload.getStatus() == 1)
                    .build();
    }

    private void saveGoods(Goods goods) {
        if (!goodsService.addGoods(goods)) {
            throw new BusinessException(BizErrorCode.GOODS_SAVE_FAILED);
        }
    }

    /**
     * 保存规格和SKU
     *
     * @param goodsId 商品ID
     * @param payload 商品发布请求对象
     */
    private void saveSpecificationsAndSkus(Long goodsId, GoodsPublishPayload payload) {
        // 保存规格名
        Map<String, Long> specNameMap = new HashMap<>();
        for (GoodsPublishPayload.SpecificationDTO spec : payload.getSpecifications()) {
            Spec specEntity = specService.getSpecByName(spec.getName());
            if (specEntity == null) {
                specEntity = addNewSpec(spec);
            }
            specNameMap.put(spec.getName(), specEntity.getId());
        }

        // 保存规格值
        Map<String, Long> specValueMap = new HashMap<>();
        for (GoodsPublishPayload.SpecificationDTO spec : payload.getSpecifications()) {
            Long specId = specNameMap.get(spec.getName());
            for (String value : spec.getValues()) {
                SpecValue specValue = specValueService.getBySpecIdAndValue(specId, value);
                if (specValue == null) {
                    specValue = addNewSpecValue(value, specId);
                }
                specValueMap.put(spec.getName() + "_" + value, specValue.getId());
            }
        }

        // 保存SKU
        List<GoodsSkuSpec> skuSpecList = new ArrayList<>();
        for (GoodsPublishPayload.SkuDTO skuDto : payload.getSkus()) {
            GoodsSku sku = buildGoodsSku(goodsId, skuDto);
            saveGoodsSku(sku);

            // 保存SKU规格关联
            for (GoodsPublishPayload.SpecValueDTO specValue : skuDto.getSpecs()) {
                String key = specValue.getName() + "_" + specValue.getValue();
                Long specValueId = specValueMap.get(key);
                if (specValueId == null) {
                    throw new BusinessException(BizErrorCode.SPEC_VALUE_INVALID);
                }

                Long specId = specNameMap.get(specValue.getName());
                GoodsSkuSpec skuSpec = buildGoodsSkuSpec(sku, specId, specValueId);
                skuSpecList.add(skuSpec);
            }
        }

        // 批量保存SKU规格关联
        if (!skuSpecList.isEmpty()) {
            skuSpecService.batchAddSpecToSku(skuSpecList);
        }
    }

    private Spec addNewSpec(GoodsPublishPayload.SpecificationDTO spec) {
        Spec specEntity;
        specEntity = new Spec();
        specEntity.setName(spec.getName());
        specEntity.setStatus(1);

        if (!specService.addSpec(specEntity)) {
            throw new BusinessException(BizErrorCode.SPEC_SAVE_FAILED);
        }
        return specEntity;
    }

    private SpecValue addNewSpecValue(String value, Long specId) {
        SpecValue specValue;
        specValue = new SpecValue();
        specValue.setSpecId(specId);
        specValue.setValue(value);
        specValue.setStatus(1);

        if (!specValueService.addValue(specValue)) {
            throw new BusinessException(BizErrorCode.SPEC_VALUE_SAVE_FAILED);
        }
        return specValue;
    }

    private GoodsSku buildGoodsSku(Long goodsId, GoodsPublishPayload.SkuDTO skuDto) {
        return GoodsSku.builder()
                       .goodsId(goodsId)
                       .price(MoneyUtil.yuanToFen(skuDto.getPrice()))
                       .inventory(skuDto.getInventory())
                       .status(skuDto.getStatus())
                       .build();
    }

    private void saveGoodsSku(GoodsSku sku) {
        if (!skuService.addSku(sku)) {
            throw new BusinessException(BizErrorCode.SKU_SAVE_FAILED);
        }
    }

    private GoodsSkuSpec buildGoodsSkuSpec(GoodsSku sku, Long specId, Long specValueId) {
        GoodsSkuSpec skuSpec = new GoodsSkuSpec();
        skuSpec.setSkuId(sku.getId());
        skuSpec.setSpecId(specId);
        skuSpec.setSpecValueId(specValueId);
        return skuSpec;
    }
}

