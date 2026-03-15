package com.onlineshop.framework.models.goods.application;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.event.MQTag;
import com.onlineshop.framework.event.MQTopicProperties;
import com.onlineshop.framework.event.TransactionCommitSendMQEvent;
import com.onlineshop.framework.exception.BizException;
import com.onlineshop.framework.models.category.Category;
import com.onlineshop.framework.models.category.ICategoryService;
import com.onlineshop.framework.models.category.vo.CategoryGoodsSectionVO;
import com.onlineshop.framework.models.category.vo.CategoryTabVO;
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
import com.onlineshop.framework.models.goods.spu.vo.GoodsCardVO;
import com.onlineshop.framework.models.goods.spu.vo.WebSpuVO;
import com.onlineshop.framework.models.store.IStoreService;
import com.onlineshop.framework.models.store.Store;
import com.onlineshop.framework.models.store.vo.StoreInfoVO;
import com.onlineshop.framework.utils.AssertUtils;
import com.onlineshop.framework.utils.AuthUserUtils;
import com.onlineshop.framework.utils.image.ImageUtil;
import com.onlineshop.framework.utils.money.Money;

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
    private static final Integer GOODS_LIMIT = 8;
    private final IGoodsService goodsService;
    private final IGoodsSkuService skuService;
    private final ISpecService specService;
    private final ISpecValueService specValueService;
    private final IGoodsSkuSpecService skuSpecService;
    private final IStoreService storeService;
    private final ICategoryService categoryService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final MQTopicProperties mqTopicProperties;

    /**
     * 发布商品（创建或更新）
     * <p>
     * 用于审核通过后或直接发布商品到平台
     * <p>
     * 处理流程：
     * 1. 根据 goodsId 判断是新增还是更新
     * 2. 构建 Goods 实体（计算最低/最高价格）
     * 3. 保存 Goods 到数据库
     * 4. 保存规格、规格值和 SKU 到数据库
     * <p>
     * 规格和规格值的处理：
     * - 自动查询或创建不存在的规格
     * - 自动查询或创建不存在的规格值
     * - 创建 SKU 与规格值的多对多关联
     *
     * @param command 商品发布命令，包含发布所需的所有数据
     * @return 发布后的商品对象
     * @throws BizException 如果发布失败
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Goods publishGoods(GoodsPublishCommand command) {
        Long goodsId = command.getGoodsId();

        Goods goods = buildGoodsFromCommand(command);
        goodsService.saveOrUpdate(goods);

        boolean isUpdate = goodsId != null;
        if (isUpdate) {
            skuService.removeByGoodsId(goodsId);
        }

        saveSpecificationsAndSkusFromCommand(goods.getId(), command);
        return goods;
    }

    /**
     * 从发布命令构建 Goods 对象
     *
     * @param command 商品发布命令
     * @return Goods 对象
     */
    private Goods buildGoodsFromCommand(GoodsPublishCommand command) {
        Map.Entry<Long, Long> minAndMaxPriceEntry = calculateSkuPriceRange(command.getSkus());
        String categoryIdPath = categoryService.buildCategoryPathByLeafCategoryId(command.getCategoryId());

        return Goods.builder()
                    .id(command.getGoodsId())
                    .name(command.getGoodsName())
                    .categoryId(command.getCategoryId())
                    .categoryIdPath(categoryIdPath)
                    .unitId(command.getUnitId())
                    .unitName(command.getUnitName())
                    .sellPoint(command.getSellPoint())
                    .displayImages(ImageUtil.joinImageUrls(command.getDisplayImageUrls()))
                    .descriptionImages(ImageUtil.joinImageUrls(command.getDescriptionImageUrls()))
                    .storeId(command.getStoreId())
                    .storeName(command.getStoreName())
                    .status(command.getStatus())
                    .minPrice(minAndMaxPriceEntry.getKey())
                    .maxPrice(minAndMaxPriceEntry.getValue())
                    .build();
    }

    /**
     * 从发布命令保存规格和 SKU
     *
     * @param goodsId 商品ID
     * @param command 商品发布命令
     */
    private void saveSpecificationsAndSkusFromCommand(Long goodsId, GoodsPublishCommand command) {
        String mainImageUrl = ImageUtil.getMainImageUrl(command.getDisplayImageUrls());
        Map<String, Long> specNameMap = createSpecNameMap(command.getSpecifications());
        Map<String, Long> specValueMap = createSpecValueMap(command.getSpecifications(), specNameMap);

        SkuSaveContext saveContext = SkuSaveContext.builder()
                                                   .goodsId(goodsId)
                                                   .skus(command.getSkus())
                                                   .specNameMap(specNameMap)
                                                   .specValueMap(specValueMap)
                                                   .goodsName(command.getGoodsName())
                                                   .mainImageUrl(mainImageUrl)
                                                   .storeId(command.getStoreId())
                                                   .build();
        saveSkusAndSpecs(saveContext);
    }

    /**
     * 计算SKU价格范围（最低价和最高价）
     * 一次遍历SKU列表，同时计算最低价和最高价，提高效率
     *
     * @param skus SKU列表
     * @return Map.Entry，key为最低价，value为最高价
     * @throws BizException 如果SKU列表为空
     */
    private Map.Entry<Long, Long> calculateSkuPriceRange(List<SkuDTO> skus) {
        if (CollUtil.isEmpty(skus)) {
            throw new BizException(BizErrorCode.SKUS_CANNOT_BE_EMPTY);
        }

        Money minPrice = null;
        Money maxPrice = null;

        for (SkuDTO sku : skus) {
            Money price = Money.ofYuan(sku.getPrice());
            if (minPrice == null || price.less(minPrice)) {
                minPrice = price;
            }
            if (maxPrice == null || price.greater(maxPrice)) {
                maxPrice = price;
            }
        }

        return new AbstractMap.SimpleEntry<>(minPrice.getCents(), maxPrice.getCents());
    }

    /**
     * 创建规格名映射表
     *
     * @param specifications 规格列表
     * @return 规格名 → 规格ID 的映射表
     */
    private Map<String, Long> createSpecNameMap(List<SpecificationsDTO> specifications) {
        Map<String, Long> specNameMap = new HashMap<>();
        for (SpecificationsDTO spec : specifications) {
            Spec specEntity = specService.lambdaQuery()
                                         .eq(Spec::getName, spec.getName())
                                         .one();
            if (specEntity == null) {
                specEntity = addNewSpec(spec);
            }
            specNameMap.put(spec.getName(), specEntity.getId());
        }
        return specNameMap;
    }

    /**
     * 创建规格值映射表
     *
     * @param specifications 规格列表
     * @param specNameMap    规格名映射表
     * @return "规格名_规格值" → 规格值ID 的映射表
     */
    private Map<String, Long> createSpecValueMap(List<SpecificationsDTO> specifications,
                                                 Map<String, Long> specNameMap) {
        Map<String, Long> specValueMap = new HashMap<>();
        for (SpecificationsDTO spec : specifications) {
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
    private void saveSkusAndSpecs(SkuSaveContext saveContext) {
        List<GoodsSkuSpec> skuSpecList = new ArrayList<>();

        for (SkuDTO skuDto : saveContext.getSkus()) {
            GoodsSku sku = buildGoodsSku(skuDto, saveContext);
            saveGoodsSku(sku);

            for (SpeValueDTO speValueDTOValue : skuDto.getSpecs()) {
                String key = speValueDTOValue.getName() + "_" + speValueDTOValue.getValue();
                Long specValueId = saveContext.getSpecValueMap()
                                              .get(key);
                if (specValueId == null) {
                    throw new BizException(BizErrorCode.SPEC_VALUE_INVALID);
                }

                Long specId = saveContext.getSpecNameMap()
                                         .get(speValueDTOValue.getName());
                GoodsSkuSpec skuSpec = buildGoodsSkuSpec(sku, specId, specValueId);
                skuSpecList.add(skuSpec);
            }
        }

        if (CollUtil.isNotEmpty(skuSpecList)) {
            skuSpecService.saveBatch(skuSpecList);
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

        if (!specService.save(specEntity)) {
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
    private GoodsSku buildGoodsSku(SkuDTO skuDTO, SkuSaveContext saveContext) {
        String specSnapshot = buildSpecSnapshot(skuDTO);
        return GoodsSku.builder()
                       .goodsId(saveContext.getGoodsId())
                       .goodsName(saveContext.getGoodsName())
                       .mainImageUrl(saveContext.getMainImageUrl())
                       .storeId(saveContext.getStoreId())
                       .specSnapshot(specSnapshot)
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

    private String buildSpecSnapshot(SkuDTO skuDTO) {
        if (CollUtil.isEmpty(skuDTO.getSpecs())) {
            return "";
        }
        return skuDTO.getSpecs()
                     .stream()
                     .map(SpeValueDTO::getValue)
                     .collect(Collectors.joining(" "));
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

        if (CollUtil.isNotEmpty(specValueIds)) {
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
     * 按分类ID查询商品（包含子分类）
     * 递归查询指定分类及其所有子分类下的商品
     *
     * @param categoryId 分类ID
     * @param limit      查询结果数量限制
     * @return 商品列表
     */
    @Override
    public List<Goods> queryGoodsByCategoryId(Long categoryId, int limit) {
        // 获取该分类及所有子分类的商品ID列表
        List<Long> categoryIds = getCategoryIdAndChildren(categoryId);

        if (CollUtil.isEmpty(categoryIds)) {
            return Collections.emptyList();
        }

        // 查询这些分类下的商品
        return goodsService.lambdaQuery()
                           .eq(Goods::getStatus, true)
                           .in(Goods::getCategoryId, categoryIds)
                           .last("limit " + limit)
                           .list();
    }

    // ==================== 私有保存方法 ====================

    @Override
    public List<CategoryGoodsSectionVO> getCategoryGoodsSections() {
        // 1. 一次性查询所有启用的分类
        List<Category> allCategories = categoryService.lambdaQuery()
                                                      .eq(Category::getStatus, true)
                                                      .list();

        if (CollUtil.isEmpty(allCategories)) {
            return Collections.emptyList();
        }

        // 2. 构建分类层级映射关系
        Map<Integer, List<Category>> levelMap = allCategories.stream()
                                                             .collect(Collectors.groupingBy(Category::getLevel));

        Map<Long, List<Category>> parentMap = allCategories.stream()
                                                           .filter(c -> c.getParentId() != null && c.getParentId() > 0)
                                                           .collect(Collectors.groupingBy(Category::getParentId));

        List<Category> firstLevelCategories = levelMap.getOrDefault(1, Collections.emptyList());
        if (CollUtil.isEmpty(firstLevelCategories)) {
            return Collections.emptyList();
        }

        // 排序一级分类
        firstLevelCategories = firstLevelCategories.stream()
                .sorted(Comparator.comparingInt(c -> c.getSort() == null ? 0 : c.getSort()))
                .collect(Collectors.toList());

        List<CategoryGoodsSectionVO> result = new ArrayList<>();

        // 3. 遍历一级分类，构建分类商品区域
        for (Category firstLevel : firstLevelCategories) {
            CategoryGoodsSectionVO section = buildCategoryGoodsSectionFast(firstLevel, parentMap);
            if (section != null) {
                result.add(section);
            }
        }
        return result;
    }

    /**
     * 快速构建一级分类的商品区域信息，利用预先构建好的 parentMap 避免查库
     */
    private CategoryGoodsSectionVO buildCategoryGoodsSectionFast(Category firstLevel, Map<Long, List<Category>> parentMap) {
        List<Category> secondLevelCategories = parentMap.getOrDefault(firstLevel.getId(), Collections.emptyList());
        if (CollUtil.isEmpty(secondLevelCategories)) {
            return null;
        }

        // 排序二级分类
        secondLevelCategories = secondLevelCategories.stream()
                .sorted(Comparator.comparingInt(c -> c.getSort() == null ? 0 : c.getSort()))
                .collect(Collectors.toList());

        // 收集这个一级分类下所有的子分类ID（包括二级、三级）用于查询商品
        List<Long> allDescendantIds = new ArrayList<>();
        allDescendantIds.add(firstLevel.getId());

        // 构建二级分类到其所有子节点ID（包括自己）的映射，用于后续划分商品
        Map<Long, List<Long>> secondLevelDescendantsMap = new HashMap<>();

        for (Category secondLevel : secondLevelCategories) {
            List<Long> secondDescendants = new ArrayList<>();
            secondDescendants.add(secondLevel.getId());

            // 获取三级分类
            List<Category> thirdLevelCategories = parentMap.getOrDefault(secondLevel.getId(), Collections.emptyList());
            for (Category thirdLevel : thirdLevelCategories) {
                secondDescendants.add(thirdLevel.getId());
            }

            secondLevelDescendantsMap.put(secondLevel.getId(), secondDescendants);
            allDescendantIds.addAll(secondDescendants);
        }

        // 去重
        List<Long> allCategoryIdsToQuery = allDescendantIds.stream().distinct().collect(Collectors.toList());

        // 一次性查询该一级分类下所有分类的商品
        int totalLimit = GOODS_LIMIT * secondLevelCategories.size();
        List<Goods> allGoods = goodsService.queryGoodsByMultipleCategoryIds(allCategoryIdsToQuery, totalLimit);

        // 构建所有二级分类的商品映射
        Map<Long, List<GoodsCardVO>> goodsMap = new HashMap<>();
        for (Category secondLevel : secondLevelCategories) {
            List<Long> relatedCategoryIds = secondLevelDescendantsMap.get(secondLevel.getId());

            List<GoodsCardVO> categoryGoods = allGoods.stream()
                                                      .filter(goods -> relatedCategoryIds.contains(goods.getCategoryId()))
                                                      .sorted((g1, g2) -> Integer.compare(
                                                              g2.getSales() != null ? g2.getSales() : 0,
                                                              g1.getSales() != null ? g1.getSales() : 0
                                                      ))
                                                      .limit(GOODS_LIMIT)
                                                      .map(GoodsCardVO::convertGoodsCardVO)
                                                      .collect(Collectors.toList());

            goodsMap.put(secondLevel.getId(), categoryGoods);
        }

        return CategoryGoodsSectionVO.builder()
                                     .category(buildCategoryTabVO(firstLevel))
                                     .tabs(buildCategoryTabVOList(secondLevelCategories))
                                     .goodsMap(goodsMap)
                                     .build();
    }

    /**
     * 构建分类标签VO
     *
     * @param category 分类实体
     * @return 分类标签VO
     */
    private CategoryTabVO buildCategoryTabVO(Category category) {
        return CategoryTabVO.builder()
                            .id(category.getId())
                            .name(category.getName())
                            .build();
    }

    /**
     * 构建分类标签VO列表
     *
     * @param categories 分类列表
     * @return 分类标签VO列表
     */
    private List<CategoryTabVO> buildCategoryTabVOList(List<Category> categories) {
        return categories.stream()
                         .map(this::buildCategoryTabVO)
                         .collect(Collectors.toList());
    }

    /**
     * 获取分类及其所有子分类的ID列表（假设最多三级分类，优化查询次数）
     *
     * @param categoryId 分类ID
     * @return 包含该分类及所有子分类的ID列表
     */
    private List<Long> getCategoryIdAndChildren(Long categoryId) {
        List<Long> allCategoryIds = new ArrayList<>();
        allCategoryIds.add(categoryId);

        // 查询二级子分类
        List<Category> children = categoryService.lambdaQuery()
                                                 .eq(Category::getParentId, categoryId)
                                                 .list();
        if (CollUtil.isEmpty(children)) {
            return allCategoryIds;
        }

        List<Long> childrenIds = children.stream()
                                         .map(Category::getId)
                                         .collect(Collectors.toList());
        allCategoryIds.addAll(childrenIds);

        // 查询三级子分类
        List<Category> grandChildren = categoryService.lambdaQuery()
                                                      .in(Category::getParentId, childrenIds)
                                                      .list();
        if (CollUtil.isNotEmpty(grandChildren)) {
            List<Long> grandChildrenIds = grandChildren.stream()
                                                       .map(Category::getId)
                                                       .collect(Collectors.toList());
            allCategoryIds.addAll(grandChildrenIds);
        }

        return allCategoryIds;
    }

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

    // ==================== Web详情相关方法 ====================

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
        if (CollUtil.isEmpty(skus)) {
            return new ArrayList<>();
        }

        // 使用LinkedHashMap保持顺序
        Map<Long, SpecificationVO> specMap = new LinkedHashMap<>();

        // 第一次遍历：收集所有规格
        for (GoodsSku sku : skus) {
            List<GoodsSkuSpec> skuSpecs = skuSpecService.listBySkuId(sku.getId());
            for (GoodsSkuSpec skuSpec : skuSpecs) {
                if (!specMap.containsKey(skuSpec.getSpecId())) {
                    Spec spec = specService.getById(skuSpec.getSpecId());
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
        if (CollUtil.isEmpty(skus)) {
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
     * 为展示模式构建规格列表
     * 从商品的所有SKU中提取规格信息，转换为SpecificationsDTO格式
     *
     * @param goodsId 商品ID
     * @return 规格DTO列表
     */
    private List<SpecificationsDTO> buildSpecificationsForDisplay(Long goodsId) {
        List<GoodsSku> skus = skuService.listByGoodsId(goodsId);
        if (CollUtil.isEmpty(skus)) {
            return new ArrayList<>();
        }

        List<GoodsSkuSpec> skuSpecs = listSkuSpecsBySkus(skus);
        if (CollUtil.isEmpty(skuSpecs)) {
            return new ArrayList<>();
        }

        Map<Long, String> specNameMap = buildSpecNameMap(skuSpecs);
        Map<Long, String> specValueMap = buildSpecValueMap(skuSpecs);
        Map<Long, LinkedHashSet<String>> groupedSpecValues = groupSpecValuesBySpecId(skuSpecs, specValueMap);
        return buildSpecificationsFromGroupedValues(groupedSpecValues, specNameMap);
    }

    private List<GoodsSkuSpec> listSkuSpecsBySkus(List<GoodsSku> skus) {
        List<Long> skuIds = skus.stream()
                                .map(GoodsSku::getId)
                                .collect(Collectors.toList());
        return skuSpecService.lambdaQuery()
                             .in(GoodsSkuSpec::getSkuId, skuIds)
                             .list();
    }

    private Map<Long, String> buildSpecNameMap(List<GoodsSkuSpec> skuSpecs) {
        Set<Long> specIds = skuSpecs.stream()
                                    .map(GoodsSkuSpec::getSpecId)
                                    .collect(Collectors.toSet());
        return specService.lambdaQuery()
                          .in(Spec::getId, specIds)
                          .list()
                          .stream()
                          .collect(Collectors.toMap(Spec::getId, Spec::getName));
    }

    private Map<Long, String> buildSpecValueMap(List<GoodsSkuSpec> skuSpecs) {
        Set<Long> specValueIds = skuSpecs.stream()
                                         .map(GoodsSkuSpec::getSpecValueId)
                                         .collect(Collectors.toSet());
        return specValueService.lambdaQuery()
                               .in(SpecValue::getId, specValueIds)
                               .list()
                               .stream()
                               .collect(Collectors.toMap(SpecValue::getId, SpecValue::getValue));
    }

    private Map<Long, LinkedHashSet<String>> groupSpecValuesBySpecId(List<GoodsSkuSpec> skuSpecs,
                                                                      Map<Long, String> specValueMap) {
        Map<Long, LinkedHashSet<String>> groupedValues = new LinkedHashMap<>();
        for (GoodsSkuSpec skuSpec : skuSpecs) {
            String specValue = specValueMap.get(skuSpec.getSpecValueId());
            if (specValue == null) {
                continue;
            }
            groupedValues.computeIfAbsent(skuSpec.getSpecId(), k -> new LinkedHashSet<>())
                         .add(specValue);
        }
        return groupedValues;
    }

    private List<SpecificationsDTO> buildSpecificationsFromGroupedValues(
            Map<Long, LinkedHashSet<String>> groupedSpecValues,
            Map<Long, String> specNameMap
    ) {
        List<SpecificationsDTO> specifications = new ArrayList<>(groupedSpecValues.size());
        for (Map.Entry<Long, LinkedHashSet<String>> entry : groupedSpecValues.entrySet()) {
            String specName = specNameMap.get(entry.getKey());
            if (specName == null) {
                continue;
            }
            specifications.add(SpecificationsDTO.builder()
                                                .name(specName)
                                                .values(new ArrayList<>(entry.getValue()))
                                                .build());
        }
        return specifications;
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
        if (CollUtil.isEmpty(skus)) {
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
            Spec spec = specService.getById(skuSpec.getSpecId());
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
        if (CollUtil.isNotEmpty(skuIds)) {
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
        if (CollUtil.isEmpty(specValueIds)) {
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
                new TransactionCommitSendMQEvent(
                        mqTopicProperties.getGoods(),
                        MQTag.GOODS_DELETE_FROM_ES,
                        goodsId
                )
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
     * 按销量排序商品并转换为GoodsCardVO
     * 销量高的排在前面
     *
     * @param goods 商品列表
     * @return 排序后的商品卡片VO列表
     */
    private List<GoodsCardVO> sortGoodsBySalesAndConvert(List<Goods> goods) {
        return goods.stream()
                    .sorted((g1, g2) -> {
                        // 销量降序排列
                        return Integer.compare(
                                g2.getSales() != null ? g2.getSales() : 0,
                                g1.getSales() != null ? g1.getSales() : 0
                        );
                    })
                    .map(GoodsCardVO::convertGoodsCardVO)
                    .collect(Collectors.toList());
    }

    @Getter
    @Builder
    private static class SkuSaveContext {
        private Long goodsId;
        private List<SkuDTO> skus;
        private Map<String, Long> specNameMap;
        private Map<String, Long> specValueMap;
        private String goodsName;
        private String mainImageUrl;
        private Long storeId;
    }
}
