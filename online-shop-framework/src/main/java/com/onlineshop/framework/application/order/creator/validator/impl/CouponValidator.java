package com.onlineshop.framework.application.order.creator.validator.impl;

import cn.hutool.core.collection.CollUtil;
import com.onlineshop.framework.models.coupon.application.ICouponAppService;
import com.onlineshop.framework.models.coupon.application.vo.CouponCalcResult;
import com.onlineshop.framework.application.order.context.TradeContext;
import com.onlineshop.framework.application.order.creator.validator.IOrderCreateValidator;
import com.onlineshop.framework.models.order.dto.TradeShopDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CouponValidator implements IOrderCreateValidator {

    @Autowired
    private ICouponAppService couponAppService;

    @Override
    public void validate(TradeContext context) {
        Map<Long, Long> shopCouponIds = new HashMap<>();
        Map<Long, Long> shopTotalPrices = new HashMap<>();
        Map<Long, Map<Long, Long>> shopItemPrices = new HashMap<>();

        for (TradeShopDTO shopDTO : context.getTradeDTO().getTradeItems()) {
            if (shopDTO.getUserCouponId() == null) {
                continue;
            }
            Long storeId = shopDTO.getStoreId();
            shopCouponIds.put(storeId, shopDTO.getUserCouponId());

            Map<Long, Long> itemPrices = new HashMap<>();
            long shopTotal = 0;
            for (var itemDTO : shopDTO.getTradeShopItemList()) {
                Long skuId = itemDTO.getSkuId();
                Long price = context.getSkuMap().get(skuId).getPrice();
                long itemTotal = price * itemDTO.getQuantity();
                itemPrices.put(skuId, itemTotal);
                shopTotal += itemTotal;
            }
            shopTotalPrices.put(storeId, shopTotal);
            shopItemPrices.put(storeId, itemPrices);
        }

        if (CollUtil.isEmpty(shopCouponIds)) {
            return;
        }

        Map<Long, CouponCalcResult> results = couponAppService.calculateDiscount(
                shopCouponIds, shopTotalPrices, shopItemPrices);
        context.setShopCouponResults(results);
    }

    @Override
    public int getOrder() {
        return 40;
    }
}
