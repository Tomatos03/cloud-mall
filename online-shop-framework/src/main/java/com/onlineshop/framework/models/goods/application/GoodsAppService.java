package com.onlineshop.framework.models.goods.application;

import cn.hutool.core.collection.CollectionUtil;
import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.event.goods.DelGoodsFromEsEvent;
import com.onlineshop.framework.event.goods.SyncGoodsToEsEvent;
import com.onlineshop.framework.exception.BizException;
import com.onlineshop.framework.models.audit.application.IAuditDelegate;
import com.onlineshop.framework.models.audit.dto.AuditSubmitDTO;
import com.onlineshop.framework.models.audit.entity.Audit;
import com.onlineshop.framework.models.audit.enums.AuditStatus;
import com.onlineshop.framework.models.audit.enums.AuditType;
import com.onlineshop.framework.models.audit.service.IAuditService;
import com.onlineshop.framework.models.category.Category;
import com.onlineshop.framework.models.category.ICategoryService;
import com.onlineshop.framework.models.goods.application.vo.AuditGoodsVO;
import com.onlineshop.framework.models.goods.application.vo.GoodsDetailVO;
import com.onlineshop.framework.models.goods.application.vo.GoodsDetailWithAuditVO;
import com.onlineshop.framework.models.goods.application.vo.WebGoodsDetailVO;
import com.onlineshop.framework.models.goods.sku.GoodsSku;
import com.onlineshop.framework.models.goods.sku.IGoodsSkuService;
import com.onlineshop.framework.models.goods.sku.SelectedSkuDTO;
import com.onlineshop.framework.models.goods.sku.SkuDTO;
import com.onlineshop.framework.models.goods.spec.dto.SpeValueDTO;
import com.onlineshop.framework.models.goods.spec.dto.SpecificationsDTO;
import com.onlineshop.framework.models.goods.spec.entity.GoodsSkuSpec;
import com.onlineshop.framework.models.goods.spec.entity.Spec;
import com.onlineshop.framework.models.goods.spec.entity.SpecValue;
import com.onlineshop.framework.models.goods.spec.service.IGoodsSkuSpecService;
import com.onlineshop.framework.models.goods.spec.service.ISpecService;
import com.onlineshop.framework.models.goods.spec.service.ISpecValueService;
import com.onlineshop.framework.models.goods.spec.vo.SpecValueVO;
import com.onlineshop.framework.models.goods.spec.vo.SpecificationVO;
import com.onlineshop.framework.models.goods.spu.Goods;
import com.onlineshop.framework.models.goods.spu.IGoodsService;
import com.onlineshop.framework.models.goods.spu.vo.WebSpuVO;
import com.onlineshop.framework.models.goods.unit.IUnitService;
import com.onlineshop.framework.models.goods.unit.Unit;
import com.onlineshop.framework.models.store.IStoreService;
import com.onlineshop.framework.models.store.Store;
import com.onlineshop.framework.models.store.vo.StoreInfoVO;
import com.onlineshop.framework.support.JsonSupport;
import com.onlineshop.framework.utils.AssertUtils;
import com.onlineshop.framework.utils.AuthUserUtils;
import com.onlineshop.framework.utils.image.ImageUtil;
import com.onlineshop.framework.utils.money.Money;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
@RequiredArgsConstructor
public class GoodsAppService implements IGoodsAppService, IAuditDelegate {
    private final IGoodsService goodsService;
    private final IGoodsSkuService skuService;
    private final ISpecService specService;
    private final ISpecValueService specValueService;
    private final IGoodsSkuSpecService skuSpecService;
    private final IAuditService auditService;
    private final ICategoryService categoryService;
    private final IUnitService unitService;
    private final IStoreService storeService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public AuditType getSupportAuditType() {
        return AuditType.GOODS;
    }

    @Override
    public void submitAudit(Object payload) {
        if (payload instanceof GoodsDTO goodsDTO) {
            validateAndFillInfo(goodsDTO);
            Long goodsId = goodsDTO.getGoodsId();
            if (goodsId != null) {
                Goods goods = goodsService.getById(goodsId);
                AuditStatus status = AuditStatus.of(goods.getAuditStatus()) == AuditStatus.APPROVED
                        ? AuditStatus.REAUDIT
                        : AuditStatus.PENDING;
                if (status != AuditStatus.REAUDIT) {
                    updateGoods(goodsDTO, status);
                } else {
                    goodsService.updateGoodsAuditStatus(goodsId, status);
                }
                auditService.updateAudit(goodsId, status, JsonSupport.toJson(goodsDTO));
            } else {
                Goods goods = addNewGoods(goodsDTO);
                AuditSubmitDTO auditSubmitDTO = buildAuditSubmit(goods.getId(), JsonSupport.toJson(goodsDTO));
                auditService.submitAudit(auditSubmitDTO);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onAuditApproved(Audit audit) {
        GoodsDTO payload = JsonSupport.fromJson(audit.getExtraInfo(), GoodsDTO.class);
        Goods goods = updateGoods(payload, AuditStatus.APPROVED);
        publishSyncGoodsToEsEvent(goods);
        auditService.updateAudit(audit.getId(), AuditStatus.APPROVED, goods.getId(), payload);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onAuditRejected(Audit audit, String reason) {
        auditService.updateAudit(audit.getId(), AuditStatus.REJECTED, audit.getTargetId(), reason);

        Goods goods = goodsService.getById(audit.getTargetId());
        goods.setAuditStatus(AuditStatus.REJECTED.getCode());
        goodsService.updateById(goods);
    }

    private Goods updateGoods(GoodsDTO payload, AuditStatus status) {
        Goods updatedGoods = createGoods(payload, status);
        goodsService.updateById(updatedGoods);

        Long goodsId = payload.getGoodsId();
        skuService.removeByGoodsId(goodsId);
        saveSpecificationsAndSkus(goodsId, payload);
        return updatedGoods;
    }

    private void publishSyncGoodsToEsEvent(Goods goods) {
        applicationEventPublisher.publishEvent(
                SyncGoodsToEsEvent.builder()
                                  .goods(goods)
                                  .build()
        );
    }

    /**
     * 构建商品对象
     */
    private Goods createGoods(GoodsDTO payload, AuditStatus status) {
        Map.Entry<Long, Long> minAndMaxPriceEntry = calculateSkuPriceRange(payload.getSkus());

        return Goods.builder()
                    .id(payload.getGoodsId())
                    .name(payload.getGoodsName())
                    .categoryId(payload.getCategoryId())
                    .categoryIdPath(payload.getCategoryIdPath())
                    .unitId(payload.getUnitId())
                    .unitName(payload.getUnitName())
                    .sellPoint(payload.getSellPoint())
                    .displayImages(ImageUtil.joinImageUrls(payload.getDisplayImageUrls()))
                    .descriptionImages(ImageUtil.joinImageUrls(payload.getDescriptionImageUrls()))
                    .storeId(payload.getStoreId())
                    .storeName(payload.getStoreName())
                    .status(payload.getStatus())
                    .minPrice(minAndMaxPriceEntry.getKey())
                    .maxPrice(minAndMaxPriceEntry.getValue())
                    .auditStatus(status.getCode())
                    .build();
    }

    /**
     * 保存规格和SKU
     */
    private void saveSpecificationsAndSkus(Long goodsId, GoodsDTO payload) {
        Map<String, Long> specNameMap = createSpecNameMap(payload);
        Map<String, Long> specValueMap = createSpecValueMap(payload, specNameMap);
        saveSkusAndSpecs(goodsId, payload, specNameMap, specValueMap);
    }

    /**
     * 计算SKU价格范围（最低价和最高价）
     * 一次遍历SKU列表，同时计算最低价和最高价，提高效率
     *
     * @param skus SKU列表
     * @return Map.Entry，key为最低价，value为最高价
     */
    private Map.Entry<Long, Long> calculateSkuPriceRange(List<SkuDTO> skus) {
        if (CollectionUtil.isEmpty(skus)) {
            throw new BizException(BizErrorCode.SKUS_CANNOT_BE_EMPTY);
        }

        String price = skus.get(0)
                           .getPrice();
        Money minPrice = Money.ofYuan(price);
        Money maxPrice = Money.ofYuan(price);

        return new AbstractMap.SimpleEntry<>(minPrice.getCents(), maxPrice.getCents());
    }

    /**
     * 创建规格名映射表
     */
    private Map<String, Long> createSpecNameMap(GoodsDTO payload) {
        Map<String, Long> specNameMap = new HashMap<>();
        for (SpecificationsDTO spec : payload.getSpecifications()) {
            Spec specEntity = specService.getSpecByName(spec.getName());
            if (specEntity == null) {
                specEntity = addNewSpec(spec);
            }
            specNameMap.put(spec.getName(), specEntity.getId());
        }
        return specNameMap;
    }

    /**
     * 创建规格值映射表
     */
    private Map<String, Long> createSpecValueMap(GoodsDTO payload,
                                                 Map<String, Long> specNameMap) {
        Map<String, Long> specValueMap = new HashMap<>();
        for (SpecificationsDTO spec : payload.getSpecifications()) {
            Long specId = specNameMap.get(spec.getName());
            for (String value : spec.getValues()) {
                SpecValue specValue = specValueService.getBySpecIdAndValue(specId, value);
                if (specValue == null) {
                    specValue = addNewSpecValue(value, specId);
                }
                specValueMap.put(spec.getName() + "_" + value, specValue.getId());
            }
        }
        return specValueMap;
    }

    /**
     * 保存SKU和规格关联
     */
    private void saveSkusAndSpecs(Long goodsId, GoodsDTO payload,
                                  Map<String, Long> specNameMap, Map<String, Long> specValueMap) {
        List<GoodsSkuSpec> skuSpecList = new ArrayList<>();

        for (SkuDTO skuDto : payload.getSkus()) {
            GoodsSku sku = buildGoodsSku(goodsId, skuDto);
            saveGoodsSku(sku);

            for (SpeValueDTO speValueDTOValue : skuDto.getSpecs()) {
                String key = speValueDTOValue.getName() + "_" + speValueDTOValue.getValue();
                Long specValueId = specValueMap.get(key);
                if (specValueId == null) {
                    throw new BizException(BizErrorCode.SPEC_VALUE_INVALID);
                }

                Long specId = specNameMap.get(speValueDTOValue.getName());
                GoodsSkuSpec skuSpec = buildGoodsSkuSpec(sku, specId, specValueId);
                skuSpecList.add(skuSpec);
            }
        }

        if (!skuSpecList.isEmpty()) {
            skuSpecService.batchAddSpecToSku(skuSpecList);
        }
    }

    /**
     * 添加新规格
     */
    private Spec addNewSpec(SpecificationsDTO spec) {
        Spec specEntity = Spec.builder()
                              .name(spec.getName())
                              .status(1)
                              .build();

        if (!specService.addSpec(specEntity)) {
            throw new BizException(BizErrorCode.SPEC_SAVE_FAILED);
        }
        return specEntity;
    }

    /**
     * 添加新规格值
     */
    private SpecValue addNewSpecValue(String value, Long specId) {
        SpecValue specValue = SpecValue.builder()
                                       .specId(specId)
                                       .value(value)
                                       .status(1)
                                       .build();

        if (!specValueService.addValue(specValue)) {
            throw new BizException(BizErrorCode.SPEC_VALUE_SAVE_FAILED);
        }
        return specValue;
    }

    /**
     * 构建SKU对象
     */
    private GoodsSku buildGoodsSku(Long goodsId, SkuDTO skuDTO) {
        return GoodsSku.builder()
                       .goodsId(goodsId)
                       .price(Money.ofYuan(skuDTO.getPrice())
                                   .getCents())
                       .inventory(skuDTO.getInventory())
                       .status(skuDTO.getStatus())
                       .build();
    }

    /**
     * 保存SKU
     */
    private void saveGoodsSku(GoodsSku sku) {
        if (!skuService.addSku(sku)) {
            throw new BizException(BizErrorCode.SKU_SAVE_FAILED);
        }
    }

    /**
     * 构建SKU规格关联对象
     */
    private GoodsSkuSpec buildGoodsSkuSpec(GoodsSku sku, Long specId, Long specValueId) {
        return GoodsSkuSpec.builder()
                           .skuId(sku.getId())
                           .specId(specId)
                           .specValueId(specValueId)
                           .build();
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
        AssertUtils.notNull(goods, BizErrorCode.GOODS_OR_SHOP_NOT_EXIST);
        AssertUtils.isEqual(AuthUserUtils.getStoreId(), goods.getStoreId(), BizErrorCode.NO_PERMISSION);

        Set<Long> specValueIds = deleteSkusAndCollectSpecValues(id);
        deleteSpu(id);

        if (!CollectionUtil.isEmpty(specValueIds)) {
            deleteSpecValueIfUnused(new ArrayList<>(specValueIds));
        }
        publishDelGoodsFromEsEvent(id);
    }

    /**
     * 获取商品详情（展示模式）
     *
     * @param id 商品ID
     * @return 商品详情
     */
    public GoodsDetailVO queryGoodsDetail(Long id) {
        Goods goods = goodsService.getById(id);
        AssertUtils.notNull(goods, BizErrorCode.GOODS_OR_SHOP_NOT_EXIST);

        return GoodsDetailVO.builder()
                            .descriptionImageUrls(ImageUtil.createImageUrlList(goods.getDescriptionImages()))
                            .specifications(buildSpecificationsForDisplay(id))
                            .skus(buildSkusForDisplay(id))
                            .build();
    }

    /**
     * 获取商品详情（包含审核信息）
     * 用于商家端查看商品详情及其关联的审核信息
     *
     * @param id 商品ID
     * @return 商品详情和审核信息
     */
    public GoodsDetailWithAuditVO getGoodsDetailWithAudit(Long id) {
        Goods goods = goodsService.getById(id);
        AssertUtils.notNull(goods, BizErrorCode.GOODS_OR_SHOP_NOT_EXIST);
        AssertUtils.isEqual(AuthUserUtils.getStoreId(), goods.getStoreId(), BizErrorCode.NO_PERMISSION);

        GoodsDetailWithAuditVO.GoodsDetailWithAuditVOBuilder builder =
                GoodsDetailWithAuditVO.builder()
                                      .descriptionImageUrls(
                                              ImageUtil.createImageUrlList(
                                                      goods.getDescriptionImages()))
                                      .specifications(
                                              buildSpecificationsForDisplay(
                                                      id))
                                      .skus(buildSkusForDisplay(
                                              id));

        Audit latestAudit = auditService.queryLatestAudit(AuditType.GOODS, id);
        if (latestAudit != null) {
            builder.auditInfo(buildAuditGoodsVO(latestAudit));
        }
        return builder.build();
    }

    /**
     * 重新发布处于撤销状态的审核商品
     * 直接修改已有审核记录的扩展信息，无需创建新的审核记录
     *
     * @param auditId 被撤销的审核记录ID
     * @param payload 新的商品发布请求对象
     */
    @Transactional(rollbackFor = Exception.class)
    public void republishGoodsFromAudit(Long auditId, GoodsDTO payload) {
        Audit audit = auditService.getById(auditId);
        AuditStatus status = AuditStatus.of(audit.getStatus());
        AssertUtils.notNull(audit, BizErrorCode.AUDIT_LOG_NOT_EXISTS);
        AssertUtils.anyTrue(
                BizErrorCode.AUDIT_INVALID_STATUS,
                AuditStatus.REVOKED == status,
                AuditStatus.REJECTED == status
        );

        Goods goods = goodsService.getById(payload.getGoodsId());
        AssertUtils.notNull(goods, BizErrorCode.GOODS_OR_SHOP_NOT_EXIST);
        AssertUtils.isEqual(AuthUserUtils.getStoreId(), goods.getStoreId(), BizErrorCode.NO_PERMISSION);

        validateAndFillInfo(payload);
        auditService.updateAudit(auditId, AuditStatus.PENDING, goods.getId(), payload);
    }

    // ==================== 私有审核方法 ====================

    private void validateAndFillInfo(GoodsDTO payload) {
        Store store = storeService.getById(payload.getStoreId());
        AssertUtils.notNull(store, BizErrorCode.GOODS_NOT_EXIST);
        payload.setStoreName(store.getName());

        Unit unit = unitService.getById(payload.getUnitId());
        AssertUtils.notNull(unit, BizErrorCode.GOODS_NOT_EXIST);
        payload.setUnitName(unit.getName());

        Category category = categoryService.queryEnableCategoryById(payload.getCategoryId());
        AssertUtils.notNull(category, BizErrorCode.CATEGORY_NOT_EXIST_OR_NO_ENABLE);
        payload.setCategoryIdPath(categoryService.buildCategoryIdPath(category.getId(), category.getParentId()));
        validateSpecificationsAndSkus(payload);
    }

    /**
     * 验证规格和SKU信息
     * 确保规格和SKU数据的完整性和有效性
     */
    private void validateSpecificationsAndSkus(GoodsDTO payload) {
        // 验证SKU中的规格引用有效性
        for (SkuDTO skuDto : payload.getSkus()) {

            // 验证SKU中的规格名称都在规格列表中
            Set<String> specNames = payload.getSpecifications()
                                           .stream()
                                           .map(SpecificationsDTO::getName)
                                           .collect(Collectors.toSet());

            for (SpeValueDTO speValueDTO : skuDto.getSpecs()) {
                if (!specNames.contains(speValueDTO.getName())) {
                    throw new BizException(BizErrorCode.SKU_SPEC_NOT_MATCH);
                }
            }
        }
    }

    // ==================== 私有保存方法 ====================

    @Override
    public WebGoodsDetailVO getWebGoodsDetail(Long id) {
        Goods goods = goodsService.getById(id);
        AssertUtils.notNull(goods, BizErrorCode.GOODS_OR_SHOP_NOT_EXIST);

        WebSpuVO spu = buildWebSpuVO(goods);
        Store store = storeService.getById(goods.getStoreId());
        StoreInfoVO storeInfo = buildStoreInfoVO(store);
        List<SpecificationVO> specifications = buildSpecificationsVOForDisplay(id);
        List<SelectedSkuDTO> selectedSkus = buildSelectedSkuDTOList(id);
        return buildWebGoodsDetailVO(spu, storeInfo, specifications, selectedSkus);
    }

    /**
     * 构建WebSpuVO
     * 将Goods转换为Web端SPU展示对象
     *
     * @param goods 商品信息
     * @return WebSpuVO
     */
    private WebSpuVO buildWebSpuVO(Goods goods) {
        List<String> displayImageUrls = ImageUtil.createImageUrlList(goods.getDisplayImages());
        List<String> descriptionImageUrls = ImageUtil.createImageUrlList(goods.getDescriptionImages());

        return WebSpuVO.builder()
                       .id(goods.getId())
                       .goodsName(goods.getName())
                       .sellPoint(goods.getSellPoint())
                       .displayImageUrls(displayImageUrls)
                       .descriptionImageUrls(descriptionImageUrls)
                       .createTime(goods.getCreateTime())
                       .sale(goods.getSales())
                       .build();
    }

    /**
     * 构建StoreInfoVO
     * 将Store转换为Web端店铺信息展示对象
     *
     * @param store 店铺
     * @return StoreInfoVO
     */
    private StoreInfoVO buildStoreInfoVO(@NonNull Store store) {
        return StoreInfoVO.builder()
                          .storeId(store.getId())
                          .storeName(store.getName())
                          .storeAvatarUrl(store.getAvatarUrl())
                          .build();
    }

    /**
     * 为展示模式构建规格列表（VO格式）
     * 从商品的所有SKU中提取规格信息，转换为SpecificationVO格式
     *
     * @param goodsId 商品ID
     * @return 规格VO列表
     */
    private List<SpecificationVO> buildSpecificationsVOForDisplay(Long goodsId) {
        List<GoodsSku> skus = skuService.listByGoodsId(goodsId);
        if (CollectionUtil.isEmpty(skus)) {
            return new ArrayList<>();
        }

        // 使用LinkedHashMap保持顺序
        Map<Long, SpecificationVO> specMap = new LinkedHashMap<>();

        // 第一次遍历：收集所有规格
        for (GoodsSku sku : skus) {
            List<GoodsSkuSpec> skuSpecs = skuSpecService.listBySkuId(sku.getId());
            for (GoodsSkuSpec skuSpec : skuSpecs) {
                if (!specMap.containsKey(skuSpec.getSpecId())) {
                    com.onlineshop.framework.models.goods.spec.entity.Spec spec =
                            specService.getSpecById(
                                    skuSpec.getSpecId());
                    if (spec != null) {
                        SpecificationVO specification = SpecificationVO.builder()
                                                                       .name(spec.getName())
                                                                       .values(new ArrayList<>())
                                                                       .build();
                        specMap.put(spec.getId(), specification);
                    }
                }
            }
        }

        // 第二次遍历：收集规格值
        for (GoodsSku sku : skus) {
            List<GoodsSkuSpec> skuSpecs = skuSpecService.listBySkuId(sku.getId());
            for (GoodsSkuSpec skuSpec : skuSpecs) {
                SpecificationVO specification = specMap.get(skuSpec.getSpecId());
                if (specification != null) {
                    SpecValue specValue = specValueService.getById(skuSpec.getSpecValueId());
                    if (specValue != null) {
                        SpecValueVO specValueVO = SpecValueVO.builder()
                                                             .id(specValue.getId())
                                                             .name(specValue.getValue())
                                                             .build();
                        // 避免重复添加相同的规格值
                        boolean exists = specification.getValues()
                                                      .stream()
                                                      .anyMatch(
                                                              v -> v.getId()
                                                                    .equals(specValueVO.getId()));
                        if (!exists) {
                            specification.getValues()
                                         .add(specValueVO);
                        }
                    }
                }
            }
        }

        return new ArrayList<>(specMap.values());
    }

    /**
     * 构建SelectedSkuDTO列表
     * 从GoodsSku和GoodsSkuSpec中直接构建SelectedSkuDTO，用于Web端显示SKU选项
     *
     * @param goodsId 商品ID
     * @return SelectedSkuDTO列表
     */
    private List<SelectedSkuDTO> buildSelectedSkuDTOList(Long goodsId) {
        List<GoodsSku> skus = skuService.listByGoodsId(goodsId);
        if (CollectionUtil.isEmpty(skus)) {
            return new ArrayList<>();
        }

        return skus.stream()
                   .map(sku -> {
                       List<GoodsSkuSpec> skuSpecs = skuSpecService.listBySkuId(sku.getId());
                       List<Long> specValueIds = skuSpecs.stream()
                                                         .map(GoodsSkuSpec::getSpecValueId)
                                                         .collect(Collectors.toList());

                       return SelectedSkuDTO.builder()
                                            .id(sku.getId())
                                            .specValueIds(specValueIds)
                                            .price(Money.ofCents(sku.getPrice())
                                                        .toYuanString())
                                            .inventory(sku.getInventory())
                                            .status(sku.getStatus())
                                            .build();
                   })
                   .collect(Collectors.toList());
    }

    /**
     * 构建WebGoodsDetailVO
     * 将SPU信息、店铺信息、规格和SKU组装成Web端返回对象
     *
     * @param spu            SPU信息
     * @param storeInfo      店铺信息
     * @param specifications 规格DTO列表
     * @param selectedSkus   选中的SKU列表
     * @return WebGoodsDetailVO
     */
    private WebGoodsDetailVO buildWebGoodsDetailVO(
            WebSpuVO spu,
            StoreInfoVO storeInfo,
            List<SpecificationVO> specifications,
            List<SelectedSkuDTO> selectedSkus
    ) {
        return WebGoodsDetailVO.builder()
                               .spu(spu)
                               .storeInfo(storeInfo)
                               .specifications(specifications)
                               .skus(selectedSkus)
                               .build();
    }

    /**
     * 构建AuditGoodsVO
     * 从Audit对象构建审核商品信息VO，包含审核状态、拒绝原因及待审核的商品信息
     *
     * @param audit 审核记录
     * @return AuditGoodsVO
     */
    private AuditGoodsVO buildAuditGoodsVO(Audit audit) {
        if (audit == null) {
            return null;
        }

        AuditStatus auditStatus = AuditStatus.of(audit.getStatus());
        String auditStatusName = auditStatus != null ? auditStatus.getName() : "未知";

        AuditGoodsVO.AuditGoodsVOBuilder builder = AuditGoodsVO.builder()
                                                               .auditId(audit.getId())
                                                               .auditStatus(audit.getStatus())
                                                               .auditStatusName(auditStatusName)
                                                               .auditReason(audit.getReason())
                                                               .auditTime(audit.getAuditTime())
                                                               .createTime(audit.getCreateTime());

        // 如果存在待审核的商品信息，则反序列化并构建PendingGoodsInfo
        if (audit.getExtraInfo() != null && !audit.getExtraInfo()
                                                  .isEmpty()) {
            GoodsDTO goodsDTO = JsonSupport.fromJson(audit.getExtraInfo(), GoodsDTO.class);
            if (goodsDTO != null) {
                AuditGoodsVO.PendingGoodsInfo pendingGoodsInfo =
                        AuditGoodsVO.PendingGoodsInfo.builder()
                                                     .displayImageUrls(goodsDTO.getDisplayImageUrls())
                                                     .descriptionImageUrls(goodsDTO.getDescriptionImageUrls())
                                                     .goodsName(goodsDTO.getGoodsName())
                                                     .sellPoint(goodsDTO.getSellPoint())
                                                     .specifications(goodsDTO.getSpecifications())
                                                     .skus(goodsDTO.getSkus())
                                                     .build();

                builder.pendingGoodsInfo(pendingGoodsInfo);
            }
        }

        return builder.build();
    }

    /**
     * 为展示模式构建规格列表
     * 从商品的所有SKU中提取规格信息，转换为SpecificationsDTO格式
     *
     * @param goodsId 商品ID
     * @return 规格DTO列表
     */
    private List<SpecificationsDTO> buildSpecificationsForDisplay(Long goodsId) {
        List<GoodsSku> skus = skuService.listByGoodsId(goodsId);
        if (CollectionUtil.isEmpty(skus)) {
            return new ArrayList<>();
        }

        // 使用LinkedHashMap保持顺序
        Map<Long, SpecificationsDTO> specMap = new LinkedHashMap<>();

        // 第一次遍历：收集所有规格
        for (GoodsSku sku : skus) {
            List<GoodsSkuSpec> skuSpecs = skuSpecService.listBySkuId(sku.getId());
            for (GoodsSkuSpec skuSpec : skuSpecs) {
                if (!specMap.containsKey(skuSpec.getSpecId())) {
                    com.onlineshop.framework.models.goods.spec.entity.Spec spec =
                            specService.getSpecById(
                                    skuSpec.getSpecId());
                    if (spec != null) {
                        SpecificationsDTO specification = SpecificationsDTO.builder()
                                                                           .name(spec.getName())
                                                                           .values(new ArrayList<>())
                                                                           .build();
                        specMap.put(spec.getId(), specification);
                    }
                }
            }
        }

        // 第二次遍历：收集规格值
        for (GoodsSku sku : skus) {
            List<GoodsSkuSpec> skuSpecs = skuSpecService.listBySkuId(sku.getId());
            for (GoodsSkuSpec skuSpec : skuSpecs) {
                SpecificationsDTO specification = specMap.get(skuSpec.getSpecId());
                if (specification != null) {
                    SpecValue specValue = specValueService.getValueById(skuSpec.getSpecValueId());
                    if (specValue != null) {
                        // 避免重复添加相同的规格值
                        boolean exists = specification.getValues()
                                                      .stream()
                                                      .anyMatch(
                                                              v -> v.equals(specValue.getValue()));
                        if (!exists) {
                            specification.getValues()
                                         .add(specValue.getValue());
                        }
                    }
                }
            }
        }

        return new ArrayList<>(specMap.values());
    }

    /**
     * 为展示模式构建SKU列表
     * 将数据库中的SKU转换为SkuDTO格式
     *
     * @param goodsId 商品ID
     * @return SKU DTO列表
     */
    private List<SkuDTO> buildSkusForDisplay(Long goodsId) {
        List<GoodsSku> skus = skuService.listByGoodsId(goodsId);
        if (CollectionUtil.isEmpty(skus)) {
            return new ArrayList<>();
        }

        return skus.stream()
                   .map(sku -> {
                       List<GoodsSkuSpec> skuSpecs = skuSpecService.listBySkuId(sku.getId());
                       List<SpeValueDTO> speValueDTOS = buildSpeValueDTOS(skuSpecs);

                       return SkuDTO.builder()
                                    .price(Money.ofCents(sku.getPrice())
                                                .toYuanString())
                                    .inventory(sku.getInventory())
                                    .status(sku.getStatus())
                                    .specs(speValueDTOS)
                                    .build();
                   })
                   .collect(Collectors.toList());
    }

    /**
     * 构建SpeValueDTO列表
     * 从GoodsSkuSpec中提取规格名和规格值
     *
     * @param skuSpecs SKU规格关联列表
     * @return SpeValueDTO列表
     */
    private List<SpeValueDTO> buildSpeValueDTOS(List<GoodsSkuSpec> skuSpecs) {
        List<SpeValueDTO> speValueDTOS = new ArrayList<>();
        for (GoodsSkuSpec skuSpec : skuSpecs) {
            com.onlineshop.framework.models.goods.spec.entity.Spec spec = specService.getSpecById(
                    skuSpec.getSpecId());
            SpecValue specValue = specValueService.getValueById(skuSpec.getSpecValueId());
            if (spec != null && specValue != null) {
                SpeValueDTO speValueDTO = SpeValueDTO.builder()
                                                     .name(spec.getName())
                                                     .value(specValue.getValue())
                                                     .build();
                speValueDTOS.add(speValueDTO);
            }
        }
        return speValueDTOS;
    }

    /**
     * 删除SKU及其关联规格值，并收集需要清理的规格值ID
     */
    private Set<Long> deleteSkusAndCollectSpecValues(Long goodsId) {
        List<GoodsSku> skus = skuService.listByGoodsId(goodsId);
        List<Long> skuIds = skus.stream()
                                .map(GoodsSku::getId)
                                .collect(Collectors.toList());

        Set<Long> specValueIds = new HashSet<>();
        if (!CollectionUtil.isEmpty(skuIds)) {
            specValueIds = collectSpecValueIdsFromSkus(skuIds);
            skuSpecService.removeBySkuIds(skuIds);
            skuService.removeByGoodsId(goodsId);
        }
        return specValueIds;
    }

    /**
     * 删除SPU记录
     */
    private void deleteSpu(Long id) {
        if (!goodsService.removeById(id)) {
            throw new BizException(BizErrorCode.SPU_DELETE_FAILED);
        }
    }

    /**
     * 删除不再被引用的规格值
     */
    private void deleteSpecValueIfUnused(List<Long> specValueIds) {
        if (CollectionUtil.isEmpty(specValueIds)) {
            return;
        }

        for (Long specValueId : specValueIds) {
            long refCount = skuSpecService.countBySpecValueId(specValueId);
            if (refCount == 0) {
                if (!specValueService.removeById(specValueId)) {
                    throw new BizException(BizErrorCode.SPEC_VALUE_DELETE_FAILED);
                }
            }
        }
    }

    private void publishDelGoodsFromEsEvent(Long goodsId) {
        applicationEventPublisher.publishEvent(
                DelGoodsFromEsEvent.builder()
                                   .goodsId(goodsId)
                                   .build()
        );
    }

    /**
     * 收集SKU关联的规格值ID
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

    /**
     * 保存新商品
     */
    private Goods addNewGoods(GoodsDTO payload) {
        Goods goods = createGoods(payload, AuditStatus.PENDING);
        goodsService.save(goods);
        saveSpecificationsAndSkus(goods.getId(), payload);
        return goods;
    }

    private static AuditSubmitDTO buildAuditSubmit(Long goodsId, String goodsJson) {
        return AuditSubmitDTO.builder()
                             .targetType(AuditType.GOODS.getCode())
                             .targetId(goodsId)
                             .extraInfo(goodsJson)
                             .build();
    }
}
