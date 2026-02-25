package com.onlineshop.framework.models.goods.application;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.event.goods.DelGoodsFromEsEvent;
import com.onlineshop.framework.exception.BizException;
import com.onlineshop.framework.models.audit.entity.Audit;
import com.onlineshop.framework.models.audit.enums.AuditStatus;
import com.onlineshop.framework.models.audit.enums.AuditType;
import com.onlineshop.framework.models.audit.service.IAuditService;
import com.onlineshop.framework.models.category.Category;
import com.onlineshop.framework.models.category.ICategoryService;
import com.onlineshop.framework.models.category.vo.CategoryGoodsSectionVO;
import com.onlineshop.framework.models.category.vo.CategoryTabVO;
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
import com.onlineshop.framework.models.goods.spu.vo.GoodsCardVO;
import com.onlineshop.framework.models.goods.spu.vo.WebSpuVO;
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
public class GoodsAppService implements IGoodsAppService {
    private static final Integer GOODS_LIMIT = 8;
    private final IGoodsService goodsService;
    private final IGoodsSkuService skuService;
    private final ISpecService specService;
    private final ISpecValueService specValueService;
    private final IGoodsSkuSpecService skuSpecService;
    private final IAuditService auditService;
    private final IStoreService storeService;
    private final ICategoryService categoryService;
    private final ApplicationEventPublisher applicationEventPublisher;

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

        // 将生成的商品ID回填到命令对象中，供后续保存规格和SKU使用
        command.setGoodsId(goods.getId());
        saveSpecificationsAndSkusFromCommand(command);
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
     * @param command 商品发布命令
     */
    private void saveSpecificationsAndSkusFromCommand(GoodsPublishCommand command) {
        Map<String, Long> specNameMap = createSpecNameMap(command.getSpecifications());
        Map<String, Long> specValueMap = createSpecValueMap(command.getSpecifications(), specNameMap);
        saveSkusAndSpecs(command.getGoodsId(), command.getSkus(), specNameMap, specValueMap);
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
        if (CollectionUtil.isEmpty(skus)) {
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
     *
     * @param goodsId      商品ID
     * @param skus         SKU列表
     * @param specNameMap  规格名映射表
     * @param specValueMap 规格值映射表
     */
    private void saveSkusAndSpecs(Long goodsId, List<SkuDTO> skus,
                                  Map<String, Long> specNameMap, Map<String, Long> specValueMap) {
        List<GoodsSkuSpec> skuSpecList = new ArrayList<>();

        for (SkuDTO skuDto : skus) {
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

    // ==================== 私有保存方法 ====================

    @Override
    public List<CategoryGoodsSectionVO> getCategoryGoodsSections() {
        // 一次性查询所有一级和二级分类
        List<Category> firstLevelCategories = categoryService.getCategoryListByLevel(1);
        List<Category> secondLevelCategories = categoryService.getCategoryListByLevel(2);

        List<CategoryGoodsSectionVO> result = new ArrayList<>();

        // 遍历一级分类，构建分类商品区域
        for (Category firstLevel : firstLevelCategories) {
            CategoryGoodsSectionVO section = buildCategoryGoodsSection(
                    firstLevel,
                    secondLevelCategories
            );
            if (section != null) {
                result.add(section);
            }
        }
        return result;
    }

    /**
     * 构建一级分类的商品区域信息
     *
     * @param firstLevel            一级分类
     * @param secondLevelCategories 所有二级分类列表
     * @return 分类商品区域VO，如果没有二级分类则返回null
     */
    private CategoryGoodsSectionVO buildCategoryGoodsSection(
            Category firstLevel,
            List<Category> secondLevelCategories
    ) {
        // 筛选属于该一级分类的所有二级分类
        List<Category> childCategories = filterChildCategoriesSorted(firstLevel.getId(), secondLevelCategories);

        if (childCategories.isEmpty()) {
            return null;
        }

        // 构建一级分类信息
        CategoryTabVO categoryTabVO = buildCategoryTabVO(firstLevel);

        // 构建二级分类标签列表
        List<CategoryTabVO> tabs = buildCategoryTabVOList(childCategories);

        // 构建所有二级分类的商品映射
        Map<Long, List<GoodsCardVO>> goodsMap = buildGoodsMapForAllChildren(childCategories);

        return CategoryGoodsSectionVO.builder()
                                     .category(categoryTabVO)
                                     .tabs(tabs)
                                     .goodsMap(goodsMap)
                                     .build();
    }

    /**
     * 筛选指定一级分类的所有二级分类，按sort排序
     *
     * @param parentId              一级分类ID
     * @param secondLevelCategories 所有二级分类列表
     * @return 筛选并排序后的二级分类列表
     */
    private List<Category> filterChildCategoriesSorted(
            Long parentId,
            List<Category> secondLevelCategories
    ) {
        return secondLevelCategories.stream()
                                    .filter(cat -> cat.getParentId()
                                                      .equals(parentId))
                                    .sorted(Comparator.comparingInt(Category::getSort))
                                    .collect(Collectors.toList());
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
      * 构建所有二级分类的商品映射 - 优化版本
      * 使用批量查询代替逐个查询，显著提升性能（减少N+1查询问题）
      *
      * 优化思路：
      * 1. 为每个二级分类构建"分类树映射"（记录该分类及其所有子分类的ID）
      * 2. 收集所有需要查询的分类ID（二级 + 三级）
      * 3. 一次查询所有商品，而不是逐个分类查询
      * 4. 在内存中按分类分组、排序、截取
      *
      * @param childCategories 二级分类列表
      * @return 分类ID -> 商品列表的映射
      */
      private Map<Long, List<GoodsCardVO>> buildGoodsMapForAllChildren(List<Category> childCategories) {
          Map<Long, List<GoodsCardVO>> goodsMap = new HashMap<>(childCategories.size());

          // 第一步：为每个二级分类构建"分类树映射表"
          // 用来记录"这个二级分类及其所有子分类"包含哪些分类ID
          Map<Long, List<Long>> categoryWithChildrenMap = new HashMap<>();
          List<Long> allCategoryIdsToQuery = new ArrayList<>();
          
          for (Category category : childCategories) {
              // 获取该分类及其所有子分类的ID（深度最多3层）
              List<Long> relatedCategoryIds = getCategoryIdAndChildren(category.getId());
              categoryWithChildrenMap.put(category.getId(), relatedCategoryIds);
              allCategoryIdsToQuery.addAll(relatedCategoryIds);
          }
          
          // 去重：可能有多个分类指向同一个子分类
          allCategoryIdsToQuery = allCategoryIdsToQuery.stream()
              .distinct()
              .collect(Collectors.toList());
          
          // 第二步：检查是否有分类需要查询
          if (allCategoryIdsToQuery.isEmpty()) {
              return goodsMap;
          }

          int totalLimit = GOODS_LIMIT * childCategories.size();
          List<Goods> allGoods = goodsService.queryGoodsByMultipleCategoryIds(
              allCategoryIdsToQuery,
              totalLimit
          );
          
          // 第四步：在内存中按分类分组和处理
          for (Category category : childCategories) {
              // 获取这个分类及其子分类的ID列表
              List<Long> relatedCategoryIds = categoryWithChildrenMap.get(category.getId());
              
              // 筛选出属于这个分类体系的商品，按销量排序，取前GOODS_LIMIT个
              List<GoodsCardVO> categoryGoods = allGoods.stream()
                  .filter(goods -> relatedCategoryIds.contains(goods.getCategoryId()))
                  .sorted((g1, g2) -> Integer.compare(
                      g2.getSales() != null ? g2.getSales() : 0,
                      g1.getSales() != null ? g1.getSales() : 0
                  ))
                  .limit(GOODS_LIMIT)
                  .map(GoodsCardVO::convertGoodsCardVO)
                  .collect(Collectors.toList());
              
              goodsMap.put(category.getId(), categoryGoods);
          }
         
         return goodsMap;
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

         if (categoryIds.isEmpty()) {
             return Collections.emptyList();
         }

         // 查询这些分类下的商品
         return goodsService.lambdaQuery()
                            .eq(Goods::getStatus, true)
                            .in(Goods::getCategoryId, categoryIds)
                            .last("limit " + limit)
                            .list();
     }

    /**
     * 获取分类及其所有子分类的ID列表（BFS算法）
     *
     * @param categoryId 分类ID
     * @return 包含该分类及所有子分类的ID列表
     */
    private List<Long> getCategoryIdAndChildren(Long categoryId) {
        List<Long> allCategoryIds = new ArrayList<>();
        allCategoryIds.add(categoryId);

        Queue<Long> queue = new LinkedList<>();
        queue.add(categoryId);

        while (!queue.isEmpty()) {
            Long currentId = queue.poll();
            List<Category> children = categoryService.list(
                    new QueryWrapper<Category>().eq("parent_id", currentId)
            );
            for (Category child : children) {
                allCategoryIds.add(child.getId());
                queue.add(child.getId());
            }
        }
        return allCategoryIds;
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

    // ==================== Web详情相关方法 ====================

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
        if (audit.getSnapshot() != null && !audit.getSnapshot()
                                                 .isEmpty()) {
            GoodsDTO goodsDTO = JsonSupport.fromJson(audit.getSnapshot(), GoodsDTO.class);
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
}
