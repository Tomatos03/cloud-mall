package com.onlineshop.framework.models.order.strategy.impl;

import com.onlineshop.framework.models.order.dto.TradeShopDTO;
import com.onlineshop.framework.models.order.dto.TradeShopItemDTO;
import com.onlineshop.framework.models.goods.spu.Goods;
import com.onlineshop.framework.models.cart.CartType;
import com.onlineshop.framework.models.order.enums.OrderStatus;
import com.onlineshop.framework.models.goods.spu.IGoodsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 立即购买订单创建策略
 * 职责：处理立即购买的订单创建逻辑，直接从商品详情页创建订单
 * 特殊逻辑：直接根据商品ID获取商品，无需验证店铺关系
 *
 * @author : Tomatos
 * @date : 2025/12/24
 */
@Slf4j
@Component
public class InstantBuyOrderCreateStrategy extends AbstractOrderCreateStrategy {

    public InstantBuyOrderCreateStrategy(IGoodsService goodsService) {
        super(goodsService);
    }

    /**
     * 获取商品信息
     * 立即购买直接根据商品ID获取商品，不需要验证店铺关系
     *
     * @param shopDTO 店铺交易数据
     * @param itemDTO 商品项DTO
     * @return 商品对象
     */
    @Override
    protected Goods getGoods(TradeShopDTO shopDTO, TradeShopItemDTO itemDTO) {
        return goodsService.getAvailableGoodsById(itemDTO.getGoodsId());
    }

    /**
     * 获取订单初始状态
     *
     * @return 待支付状态
     */
    @Override
    protected String getOrderStatus() {
        return OrderStatus.CREATED.getCode();
    }

    @Override
    public CartType getSupportedCartType() {
        return CartType.INSTANT_BUY;
    }
}