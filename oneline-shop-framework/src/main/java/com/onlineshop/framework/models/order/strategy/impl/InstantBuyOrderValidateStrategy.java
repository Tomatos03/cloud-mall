package com.onlineshop.framework.models.order.strategy.impl;

import com.onlineshop.framework.models.cart.CartType;
import com.onlineshop.framework.models.goods.spu.IGoodsService;
import com.onlineshop.framework.models.goods.sku.IGoodsSkuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 立即购买订单校验策略
 * 职责：在订单创建前完成所有必要的数据校验
 * 
 * 多规格商品支持更新（2025-01-02）:
 * - 从商品级别验证 → SKU级别验证
 *
 * @author : Tomatos
 * @date : 2025/12/24
 */
@Slf4j
@Component
public class InstantBuyOrderValidateStrategy extends AbstractOrderValidateStrategy {

    public InstantBuyOrderValidateStrategy(
            IGoodsService goodsService,
            IGoodsSkuService goodsSkuService) {
        super(goodsService, goodsSkuService);
    }

    @Override
    public CartType supportCartType() {
        return CartType.INSTANT_BUY;
    }
}