package com.onlineshop.framework.models.goods.sku;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.models.goods.spec.entity.GoodsSkuSpec;
import com.onlineshop.framework.models.goods.spec.entity.SpecValue;
import com.onlineshop.framework.models.goods.spec.service.IGoodsSkuSpecService;
import com.onlineshop.framework.models.goods.spec.service.ISpecValueService;
import com.onlineshop.framework.models.goods.spu.Goods;
import com.onlineshop.framework.models.goods.spu.IGoodsService;
import com.onlineshop.framework.utils.AssertUtils;
import com.onlineshop.framework.utils.AuthUserUtils;
import com.onlineshop.framework.utils.image.ImageUtil;
import com.onlineshop.framework.utils.money.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoodsSkuService extends ServiceImpl<GoodsSkuMapper, GoodsSku> implements IGoodsSkuService {
    private final IGoodsService goodsService;
    private final IGoodsSkuSpecService goodsSkuSpecService;
    private final ISpecValueService specValueService;

    @Override
    public List<GoodsSku> listByGoodsId(Long goodsId) {
        return lambdaQuery().eq(GoodsSku::getGoodsId, goodsId)
                            .list();
    }

    @Override
    public boolean addSku(GoodsSku goodsSku) {
        return save(goodsSku);
    }

    @Override
    public void removeByGoodsId(Long goodsId) {
        baseMapper.delete(
                new LambdaQueryWrapper<GoodsSku>().eq(GoodsSku::getGoodsId, goodsId)
        );
    }

    @Override
    public IPage<MerchantGoodsSkuItemDTO> pageMerchantGoodsSkus(MerchantGoodsSkuParamsDTO params) {
        AssertUtils.notNull(params, BizErrorCode.INVALID_PARAM);
        Long storeId = AuthUserUtils.getStoreId();
        AssertUtils.notNull(storeId, BizErrorCode.STORE_NOT_EXIST);

        List<Goods> goodsList = goodsService.lambdaQuery()
                                            .eq(Goods::getStoreId, storeId)
                                            .eq(params.getGoodsId() != null, Goods::getId, params.getGoodsId())
                                            .select(Goods::getId, Goods::getName, Goods::getDisplayImages)
                                            .list();
        if (CollUtil.isEmpty(goodsList)) {
            return new Page<>(params.getPage(), params.getPageSize(), 0);
        }

        List<Long> goodsIds = goodsList.stream()
                                       .map(Goods::getId)
                                       .collect(Collectors.toList());
        Map<Long, Goods> goodsMap = goodsList.stream()
                                             .collect(Collectors.toMap(Goods::getId, Function.identity()));

        Page<GoodsSku> page = new Page<>(params.getPage(), params.getPageSize());
        IPage<GoodsSku> skuPage = lambdaQuery().in(GoodsSku::getGoodsId, goodsIds)
                                               .orderByDesc(GoodsSku::getCreateTime)
                                               .page(page);
        if (CollUtil.isEmpty(skuPage.getRecords())) {
            return new Page<>(params.getPage(), params.getPageSize(), skuPage.getTotal());
        }

        Map<Long, List<GoodsSkuSpec>> skuSpecMap = querySkuSpecMap(skuPage.getRecords());
        Map<Long, String> specValueMap = querySpecValueMap(skuSpecMap);

        return skuPage.convert(sku -> convertToPageItem(sku, goodsMap.get(sku.getGoodsId()),
                                                        skuSpecMap.get(sku.getId()), specValueMap));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductInventoryAndIncreaseSales(Long skuId, Integer quantity) {
        GoodsSku sku = baseMapper.selectById(skuId);
        if (sku == null || sku.getInventory() < quantity) {
            log.warn("SKU不存在或库存不足, skuId: {}, inventory: {}, quantity: {}",
                     skuId, sku != null ? sku.getInventory() : null, quantity);
            return;
        }

        // 扣减库存
        sku.setInventory(sku.getInventory() - quantity);
        // 增加销量
        sku.setSales(sku.getSales() + quantity);

        boolean result = updateById(sku);
        if (result) {
            log.info("SKU库存扣减和销量更新成功, skuId: {}, 扣减库存: {}, 增加销量: {}",
                     skuId, quantity, quantity);
        }
    }

    private Map<Long, List<GoodsSkuSpec>> querySkuSpecMap(List<GoodsSku> skus) {
        if (CollUtil.isEmpty(skus)) {
            return Collections.emptyMap();
        }

        List<Long> skuIds = skus.stream()
                                .map(GoodsSku::getId)
                                .collect(Collectors.toList());
        List<GoodsSkuSpec> skuSpecs = goodsSkuSpecService.lambdaQuery()
                                                         .in(GoodsSkuSpec::getSkuId, skuIds)
                                                         .list();
        if (CollUtil.isEmpty(skuSpecs)) {
            return Collections.emptyMap();
        }
        return skuSpecs.stream()
                       .collect(Collectors.groupingBy(GoodsSkuSpec::getSkuId));
    }

    private Map<Long, String> querySpecValueMap(Map<Long, List<GoodsSkuSpec>> skuSpecMap) {
        if (skuSpecMap.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<Long> specValueIds = skuSpecMap.values()
                                           .stream()
                                           .flatMap(List::stream)
                                           .map(GoodsSkuSpec::getSpecValueId)
                                           .collect(Collectors.toSet());
        if (CollUtil.isEmpty(specValueIds)) {
            return Collections.emptyMap();
        }

        List<SpecValue> specValues = specValueService.listByIds(specValueIds);
        if (CollUtil.isEmpty(specValues)) {
            return Collections.emptyMap();
        }
        return specValues.stream()
                         .collect(Collectors.toMap(SpecValue::getId, SpecValue::getValue));
    }

    private MerchantGoodsSkuItemDTO convertToPageItem(GoodsSku sku, Goods goods, List<GoodsSkuSpec> skuSpecs,
                                                      Map<Long, String> specValueMap) {
        return MerchantGoodsSkuItemDTO.builder()
                                      .skuId(String.valueOf(sku.getId()))
                                      .goodsName(goods == null ? null : goods.getName())
                                      .imageUrl(resolveMainImageUrl(goods))
                                      .price(convertYuanPrice(sku.getPrice()))
                                      .specs(buildSpecs(skuSpecs, specValueMap))
                                      .inventory(sku.getInventory())
                                      .build();
    }

    private String resolveMainImageUrl(Goods goods) {
        if (goods == null || StrUtil.isBlank(goods.getDisplayImages())) {
            return null;
        }
        return ImageUtil.getMainImageUrl(goods.getDisplayImages());
    }

    private String convertYuanPrice(Long cents) {
        if (cents == null) {
            return Money.ofCents(0).toYuanString();
        }
        return Money.ofCents(cents)
                    .toYuanString();
    }

    private List<String> buildSpecs(List<GoodsSkuSpec> skuSpecs, Map<Long, String> specValueMap) {
        if (CollUtil.isEmpty(skuSpecs)) {
            return Collections.emptyList();
        }

        List<String> specs = new ArrayList<>();
        List<GoodsSkuSpec> sortedSkuSpecs = new ArrayList<>(skuSpecs);
        sortedSkuSpecs.sort(Comparator.comparing(GoodsSkuSpec::getSpecId));
        for (GoodsSkuSpec skuSpec : sortedSkuSpecs) {
            String specValue = specValueMap.get(skuSpec.getSpecValueId());
            if (StrUtil.isBlank(specValue)) {
                continue;
            }
            specs.add(specValue);
        }
        return specs;
    }
}
