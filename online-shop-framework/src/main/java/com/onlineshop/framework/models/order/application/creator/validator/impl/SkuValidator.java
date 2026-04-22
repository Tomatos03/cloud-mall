package com.onlineshop.framework.models.order.application.creator.validator.impl;

import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.models.goods.sku.GoodsSku;
import com.onlineshop.framework.models.goods.sku.IGoodsSkuService;
import com.onlineshop.framework.models.order.application.context.TradeContext;
import com.onlineshop.framework.models.order.application.creator.validator.IOrderCreateValidator;
import com.onlineshop.framework.models.order.dto.TradeShopDTO;
import com.onlineshop.framework.models.order.dto.TradeShopItemDTO;
import com.onlineshop.framework.utils.AssertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class SkuValidator implements IOrderCreateValidator {

    private final IGoodsSkuService goodsSkuService;

    @Autowired
    public SkuValidator(IGoodsSkuService goodsSkuService) {
        this.goodsSkuService = goodsSkuService;
    }

    @Override
    public void validate(TradeContext context) {
        Set<Long> skuIdSet = context.getTradeDTO()
                                    .getTradeItems()
                                    .stream()
                                    .map(TradeShopDTO::getTradeShopItemList)
                                    .flatMap(Collection::stream)
                                    .map(TradeShopItemDTO::getSkuId)
                                    .collect(Collectors.toSet());

        AssertUtils.notEmpty(skuIdSet, BizErrorCode.ORDER_CREATE_ITEMS_EMPTY);

        Map<Long, Integer> skuDemandMap = context.getTradeDTO()
                                                 .getTradeItems()
                                                 .stream()
                                                 .map(TradeShopDTO::getTradeShopItemList)
                                                 .flatMap(Collection::stream)
                                                 .collect(Collectors.toMap(
                                                         TradeShopItemDTO::getSkuId,
                                                         TradeShopItemDTO::getQuantity,
                                                         Integer::sum,
                                                         HashMap::new
                                                 ));

        List<GoodsSku> skuList = goodsSkuService.lambdaQuery()
                                                .in(GoodsSku::getId, skuIdSet)
                                                .eq(GoodsSku::getStatus, Boolean.TRUE)
                                                .list();
        AssertUtils.isTrue(skuList.size() == skuIdSet.size(), BizErrorCode.ORDER_CREATE_SKU_NOT_AVAILABLE);

        Map<Long, GoodsSku> skuMap = skuList.stream()
                                            .collect(Collectors.toMap(
                                                    GoodsSku::getId,
                                                    sku -> sku,
                                                    (existing, replacement) -> existing,
                                                    HashMap::new
                                            ));

        validateSkuStore(context, skuMap);
        validateSkuInventory(skuMap, skuDemandMap);
        context.setSkuMap(skuMap);
    }

    @Override
    public int getOrder() {
        return 30;
    }

    private void validateSkuStore(TradeContext context, Map<Long, GoodsSku> skuMap) {
        for (TradeShopDTO shopDTO : context.getTradeDTO()
                                           .getTradeItems()) {
            for (TradeShopItemDTO itemDTO : shopDTO.getTradeShopItemList()) {
                GoodsSku sku = skuMap.get(itemDTO.getSkuId());
                AssertUtils.notNull(sku, BizErrorCode.ORDER_CREATE_SKU_NOT_AVAILABLE);
                AssertUtils.isTrue(shopDTO.getStoreId()
                                          .equals(sku.getStoreId()), BizErrorCode.ORDER_CREATE_SKU_STORE_MISMATCH);
            }
        }
    }

    private void validateSkuInventory(Map<Long, GoodsSku> skuMap, Map<Long, Integer> skuDemandMap) {
        for (Map.Entry<Long, Integer> skuDemandEntry : skuDemandMap.entrySet()) {
            GoodsSku sku = skuMap.get(skuDemandEntry.getKey());
            AssertUtils.isTrue(
                    sku.getInventory() >= skuDemandEntry.getValue(),
                    BizErrorCode.ORDER_CREATE_SKU_INVENTORY_NOT_ENOUGH
            );
        }
    }
}