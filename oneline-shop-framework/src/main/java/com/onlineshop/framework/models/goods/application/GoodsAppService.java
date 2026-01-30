package com.onlineshop.framework.models.goods.application;

import cn.hutool.core.collection.CollectionUtil;
import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.event.goods.DelGoodsFromEsEvent;
import com.onlineshop.framework.event.goods.SyncGoodsToEsEvent;
import com.onlineshop.framework.exception.BusinessException;
import com.onlineshop.framework.models.audit.dto.AuditSubmitDTO;
import com.onlineshop.framework.models.audit.entity.Audit;
import com.onlineshop.framework.models.audit.enums.AuditStatus;
import com.onlineshop.framework.models.audit.enums.AuditType;
import com.onlineshop.framework.models.audit.service.IAuditService;
import com.onlineshop.framework.models.category.Category;
import com.onlineshop.framework.models.category.ICategoryService;
import com.onlineshop.framework.models.goods.application.vo.GoodsDetailVO;
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
import com.onlineshop.framework.models.store.IStoreService;
import com.onlineshop.framework.models.store.Store;
import com.onlineshop.framework.models.store.vo.StoreInfoVO;
import com.onlineshop.framework.models.unit.IUnitService;
import com.onlineshop.framework.models.unit.Unit;
import com.onlineshop.framework.support.JsonSupport;
import com.onlineshop.framework.utils.context.UserContextHolder;
import com.onlineshop.framework.utils.image.ImageUtil;
import com.onlineshop.framework.utils.money.Money;
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
public class GoodsAppService implements IGoodsAppService {
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
    public void submitGoodsAudit(GoodsDTO payload) {
        validateAndFillInfo(payload);
        if (!isNewGoods(payload)) {
            // 校验现有商品存在且有权限访问
            Goods existingGoods = goodsService.getById(payload.getGoodsId());
            checkGoods(existingGoods);
            checkMerchantOwnership(existingGoods);
        }
        submitAuditRequest(payload);
    }

    private void validateAndFillInfo(GoodsDTO payload) {
        validateAndFillStoreInfo(payload);
        validateAndFillUnitInfo(payload);
        validateAndFillCategoryInfo(payload);
        validateSpecificationsAndSkus(payload);
    }

    /**
     * 判断是否为新商品
     */
    private boolean isNewGoods(GoodsDTO payload) {
        return payload.getGoodsId() == null || payload.getGoodsId() == 0;
    }

    /**
     * 验证商品存在
     */
    private void checkGoods(Goods goods) {
        if (goods == null) {
            throw new BusinessException(BizErrorCode.GOODS_OR_SHOP_NOT_EXIST);
        }
    }

    /**
     * 验证商家所有权
     */
    private void checkMerchantOwnership(Goods goods) {
        Long currentStoreId = UserContextHolder.getStoreId();
        if (!Objects.equals(currentStoreId, goods.getStoreId())) {
            throw new BusinessException(BizErrorCode.NO_PERMISSION);
        }
    }

    /**
     * 提交审核申请
     */
    private void submitAuditRequest(GoodsDTO payload) {
        String payloadJson = JsonSupport.toJson(payload);
        Long targetId = isNewGoods(payload) ? 0L : payload.getGoodsId();

        AuditSubmitDTO auditSubmitDTO = AuditSubmitDTO.builder()
                                                      .targetType(AuditType.GOODS.getCode())
                                                      .targetId(targetId)
                                                      .extraInfo(payloadJson)
                                                      .build();

        auditService.submitAudit(auditSubmitDTO);
    }

    private void validateAndFillStoreInfo(GoodsDTO payload) {
        Store store = storeService.lambdaQuery()
                                  .eq(Store::getId, payload.getStoreId())
                                  .one();
        if (store == null) {
            throw new BusinessException(BizErrorCode.GOODS_OR_SHOP_NOT_EXIST);
        }
        payload.setStoreName(store.getName());
    }

    /**
     * 校验并填充单位信息
     * 验证单位是否存在，同时填充单位名称到payload中
     */
    private void validateAndFillUnitInfo(GoodsDTO payload) {
        Unit unit = unitService.getById(payload.getUnitId());
        if (unit == null || unit.getStatus() != 1) {
            throw new BusinessException(BizErrorCode.GOODS_OR_SHOP_NOT_EXIST);
        }
        // 填充单位名称
        payload.setUnitName(unit.getName());
    }

    private void validateAndFillCategoryInfo(GoodsDTO payload) {
        Category category = categoryService.lambdaQuery()
                                           .eq(Category::getId, payload.getCategoryId())
                                           .eq(Category::getStatus, true)
                                           .one();
        if (category == null) {
            throw new BusinessException(BizErrorCode.CATEGORY_NOT_EXIST);
        }

        payload.setCategoryIdPath(
                categoryService.getCategoryIdPath(category.getId(), category.getParentId())
        );
    }

    /**
     * 验证规格和SKU信息
     * 确保规格和SKU数据的完整性和有效性
     */
    private void validateSpecificationsAndSkus(GoodsDTO payload) {
        if (CollectionUtil.isEmpty(payload.getSpecifications())) {
            throw new BusinessException(BizErrorCode.SPECIFICATIONS_CANNOT_BE_EMPTY);
        }

        if (CollectionUtil.isEmpty(payload.getSkus())) {
            throw new BusinessException(BizErrorCode.SKUS_CANNOT_BE_EMPTY);
        }

        // 验证规格数量限制
        if (payload.getSpecifications()
                   .size() > 3) {
            throw new BusinessException(BizErrorCode.SPECIFICATIONS_EXCEED_MAX_LIMIT);
        }

        // 验证SKU中的规格引用有效性
        for (SkuDTO skuDto : payload.getSkus()) {
            if (CollectionUtil.isEmpty(skuDto.getSpecs())) {
                throw new BusinessException(BizErrorCode.SKU_SPECS_CANNOT_BE_EMPTY);
            }

            // 验证SKU中的规格名称都在规格列表中
            Set<String> specNames = payload.getSpecifications()
                                           .stream()
                                           .map(SpecificationsDTO::getName)
                                           .collect(Collectors.toSet());

            for (SpeValueDTO speValueDTO : skuDto.getSpecs()) {
                if (!specNames.contains(speValueDTO.getName())) {
                    throw new BusinessException(BizErrorCode.SKU_SPEC_NOT_MATCH);
                }
            }
        }
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

        Set<Long> specValueIds = deleteSkusAndCollectSpecValues(id);
        deleteSpu(id);

        if (!CollectionUtil.isEmpty(specValueIds)) {
            deleteSpecValueIfUnused(new ArrayList<>(specValueIds));
        }
        publishDelGoodsFromEsEvent(id);
    }

    private void publishDelGoodsFromEsEvent(Long goodsId) {
        applicationEventPublisher.publishEvent(
                DelGoodsFromEsEvent.builder()
                                   .goodsId(goodsId)
                                   .build()
        );
    }

    /**
     * 根据审核记录实际保存商品
     * 从审核表恢复商品信息，进行实际的数据库保存或更新操作
     *
     * @param auditId 审核记录ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateGoodsAfterAudit(Long auditId) {
        Audit audit = getAndValidateAudit(auditId);
        GoodsDTO payload = deserializePayload(audit);

        if (isNewGoods(payload)) {
            saveNewGoods(payload);
        } else {
            updateExistingGoods(payload);
        }

        publishSyncGoodsToEsEvent(new Goods());
    }

    private void publishSyncGoodsToEsEvent(Goods goods) {
        applicationEventPublisher.publishEvent(
                SyncGoodsToEsEvent.builder()
                                  .goods(goods)
                                  .build()
        );
    }

    // ==================== 私有验证方法 ====================

    /**
     * 获取商品详情（展示模式）
     *
     * @param id 商品ID
     * @return 商品详情
     */
    public GoodsDetailVO getGoodsDetail(Long id) {
        Goods goods = goodsService.getById(id);
        checkGoods(goods);

        return GoodsDetailVO.builder()
                            .descriptionImageUrls(
                                    ImageUtil.createImageUrlList(goods.getDescriptionImages()))
                            .specifications(buildSpecificationsForDisplay(id))
                            .skus(buildSkusForDisplay(id))
                            .build();
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
        // 验证审核记录
        Audit audit = getAndValidateAudit(auditId);
        validateAuditStatus(audit);
        validateAndFillInfo(payload);

        if (!isNewGoods(payload)) {
            Goods existingGoods = goodsService.getById(payload.getGoodsId());
            checkGoods(existingGoods);
            checkMerchantOwnership(existingGoods);
        }

        // 序列化新的商品信息为JSON
        String payloadJson = JsonSupport.toJson(payload);

        auditService.updateById(
                Audit.builder()
                     .id(auditId)
                     .extraInfo(payloadJson)
                     .reason(null)
                     .status(AuditStatus.PENDING.getCode())
                     .build()
        );
    }

    @Override
    public WebGoodsDetailVO getWebGoodsDetail(Long id) {
        Goods goods = goodsService.getById(id);
        checkGoods(goods);

        WebSpuVO spu = buildWebSpuVO(goods);
        StoreInfoVO storeInfo = buildStoreInfoVO(goods.getStoreId());
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
     * @param storeId 店铺ID
     * @return StoreInfoVO
     */
    private StoreInfoVO buildStoreInfoVO(Long storeId) {
        Store store = storeService.getById(storeId);
        if (store == null) {
            return null;
        }

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
                                            .price(Money.ofCents(sku.getPrice()).toYuanString())
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
     * @param spu SPU信息
     * @param storeInfo 店铺信息
     * @param specifications 规格DTO列表
     * @param selectedSkus 选中的SKU列表
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
     * 获取并验证审核记录
     */
    private Audit getAndValidateAudit(Long auditId) {
        if (auditId == null) {
            throw new BusinessException(BizErrorCode.AUDIT_ID_CANNOT_BE_NULL);
        }
        Audit audit = auditService.getById(auditId);
        if (audit == null) {
            throw new BusinessException(BizErrorCode.AUDIT_LOG_NOT_EXISTS);
        }
        return audit;
    }

    /**
     * 验证审核记录是否为已撤销状态
     * 只有被撤销的审核记录才能重新发布
     */
    private void validateAuditStatus(Audit audit) {
        AuditStatus auditStatus = AuditStatus.of(audit.getStatus());
        if (
                AuditStatus.REVOKED == auditStatus
                        || AuditStatus.REJECTED == auditStatus
        ) {
            return;
        }
        throw new BusinessException(BizErrorCode.AUDIT_INVALID_STATUS);
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

    // ==================== 私有审核方法 ====================

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

    // ==================== 私有保存方法 ====================

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
            throw new BusinessException(BizErrorCode.SPU_DELETE_FAILED);
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
                    throw new BusinessException(BizErrorCode.SPEC_VALUE_DELETE_FAILED);
                }
            }
        }
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
     * 添加新规格
     */
    private Spec addNewSpec(SpecificationsDTO spec) {
        Spec specEntity = Spec.builder()
                              .name(spec.getName())
                              .status(1)
                              .build();

        if (!specService.addSpec(specEntity)) {
            throw new BusinessException(BizErrorCode.SPEC_SAVE_FAILED);
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
            throw new BusinessException(BizErrorCode.SPEC_VALUE_SAVE_FAILED);
        }
        return specValue;
    }

    /**
     * 构建SKU对象
     */
    private GoodsSku buildGoodsSku(Long goodsId, SkuDTO skuDTO) {
        return GoodsSku.builder()
                       .goodsId(goodsId)
                       .price(Money.ofYuan(skuDTO.getPrice()).getCents())
                       .inventory(skuDTO.getInventory())
                       .status(skuDTO.getStatus())
                       .build();
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
     * 计算SKU价格范围（最低价和最高价）
     * 一次遍历SKU列表，同时计算最低价和最高价，提高效率
     *
     * @param skus SKU列表
     * @return Map.Entry，key为最低价，value为最高价
     */
    private Map.Entry<Long, Long> calculateSkuPriceRange(List<SkuDTO> skus) {
        if (CollectionUtil.isEmpty(skus)) {
            throw new BusinessException(BizErrorCode.SKUS_CANNOT_BE_EMPTY);
        }

        String price = skus.get(0).getPrice();
        Money minPrice = Money.ofYuan(price);
        Money maxPrice = Money.ofYuan(price);

        return new AbstractMap.SimpleEntry<>(minPrice.getCents(), maxPrice.getCents());
    }

    /**
     * 构建商品对象
     */
    private Goods createGoods(GoodsDTO payload) {
        Map.Entry<Long, Long> minAndMaxPriceEntry = calculateSkuPriceRange(payload.getSkus());

        return Goods.builder()
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
                    .build();
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
     * 从审核记录恢复商品信息
     */
    private GoodsDTO deserializePayload(Audit audit) {
        String extraInfo = audit.getExtraInfo();
        if (extraInfo == null || extraInfo.isEmpty()) {
            throw new BusinessException(BizErrorCode.AUDIT_EXTRA_INFO_EMPTY);
        }

        GoodsDTO payload = JsonSupport.fromJson(extraInfo, GoodsDTO.class);
        if (payload == null) {
            throw new BusinessException(BizErrorCode.GOODS_PUBLISH_PAYLOAD_INVALID);
        }
        return payload;
    }

    /**
     * 保存商品基本信息
     */
    private void saveGoods(Goods goods) {
        if (!goodsService.addGoods(goods)) {
            throw new BusinessException(BizErrorCode.GOODS_SAVE_FAILED);
        }
    }

    /**
     * 保存SKU
     */
    private void saveGoodsSku(GoodsSku sku) {
        if (!skuService.addSku(sku)) {
            throw new BusinessException(BizErrorCode.SKU_SAVE_FAILED);
        }
    }

    /**
     * 保存新商品
     */
    private void saveNewGoods(GoodsDTO payload) {
        Goods goods = createGoods(payload);
        saveGoods(goods);
        saveSpecificationsAndSkus(goods.getId(), payload);
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
                    throw new BusinessException(BizErrorCode.SPEC_VALUE_INVALID);
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
     * 保存规格和SKU
     */
    private void saveSpecificationsAndSkus(Long goodsId, GoodsDTO payload) {
        Map<String, Long> specNameMap = createSpecNameMap(payload);
        Map<String, Long> specValueMap = createSpecValueMap(payload, specNameMap);
        saveSkusAndSpecs(goodsId, payload, specNameMap, specValueMap);
    }

    /**
     * 更新现有商品
     */
    private void updateExistingGoods(GoodsDTO payload) {
        Long goodsId = payload.getGoodsId();
        Goods existingGoods = goodsService.getById(goodsId);
        checkGoods(existingGoods);

        Goods goods = createGoods(payload);
        updateGoodsEntity(goods);

        skuService.removeByGoodsId(goodsId);
        saveSpecificationsAndSkus(goodsId, payload);
    }

    /**
     * 更新商品基本信息
     */
    private void updateGoodsEntity(Goods goods) {
        if (!goodsService.updateGoods(goods)) {
            throw new BusinessException(BizErrorCode.GOODS_UPDATE_FAILED);
        }
    }
}
