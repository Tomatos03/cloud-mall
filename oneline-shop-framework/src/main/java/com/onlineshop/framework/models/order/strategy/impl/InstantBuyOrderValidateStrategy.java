package com.onlineshop.framework.models.order.strategy.impl;

import com.onlineshop.framework.models.cart.CartType;
import com.onlineshop.framework.models.goods.IGoodsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 立即购买订单校验策略
 * 职责：在订单创建前完成所有必要的数据校验
 *
 * @author : Tomatos
 * @date : 2025/12/24
 */
@Slf4j
@Component
public class InstantBuyOrderValidateStrategy extends AbstractOrderValidateStrategy {

    public InstantBuyOrderValidateStrategy(IGoodsService goodsService) {
        super(goodsService);
    }

    @Override
    public CartType supportCartType() {
        return CartType.INSTANT_BUY;
    }
}