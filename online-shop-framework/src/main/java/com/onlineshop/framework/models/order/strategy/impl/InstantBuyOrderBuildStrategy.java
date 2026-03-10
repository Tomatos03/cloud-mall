package com.onlineshop.framework.models.order.strategy.impl;

import com.onlineshop.framework.models.cart.CartType;
import com.onlineshop.framework.models.goods.sku.GoodsSku;
import com.onlineshop.framework.models.goods.sku.IGoodsSkuService;
import com.onlineshop.framework.models.goods.spec.service.IGoodsSkuSpecService;
import com.onlineshop.framework.models.goods.spec.service.ISpecService;
import com.onlineshop.framework.models.goods.spec.service.ISpecValueService;
import com.onlineshop.framework.models.goods.spu.Goods;
import com.onlineshop.framework.models.goods.spu.IGoodsService;
import com.onlineshop.framework.models.order.dto.TradeShopDTO;
import com.onlineshop.framework.models.order.dto.TradeShopItemDTO;
import com.onlineshop.framework.models.order.enums.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 立即购买订单创建策略
 * 职责：处理立即购买的订单创建逻辑，直接从商品详情页创建订单
 * 特点：基于具体的 SKU 进行下单，需要先从 SKU 获取 goodsId，然后获取商品信息
 * 
 * 前提：商品和库存校验已在 Validate 策略中完成
 *
 * @author : Tomatos
 * @date : 2025/12/24
 */
@Slf4j
@Component
public class InstantBuyOrderBuildStrategy extends AbstractOrderBuildStrategy {

    public InstantBuyOrderBuildStrategy(
            IGoodsService goodsService,
            IGoodsSkuService goodsSkuService,
            IGoodsSkuSpecService goodsSkuSpecService,
            ISpecService specService,
            ISpecValueService specValueService
    ) {
        super(goodsService, goodsSkuService, goodsSkuSpecService, specService, specValueService);
    }

    /**
     * 获取商品信息
     * 流程：skuId -> SKU -> goodsId -> Goods
     * 
     * 注意：itemDTO 中的 skuId 是真实的下单单位，不是商品 ID
     * 需要先从 SKU 获取 goodsId，然后再根据 goodsId 获取完整的商品信息
     * 
     * 数据已通过校验，SKU 和商品一定存在
     *
     * @param shopDTO 店铺交易数据
     * @param itemDTO 商品项DTO（包含 skuId）
     * @return 商品对象
     */
    @Override
    protected Goods getGoods(TradeShopDTO shopDTO, TradeShopItemDTO itemDTO) {
        // 根据 SKU ID 获取 SKU 信息
        GoodsSku sku = goodsSkuService.getById(itemDTO.getSkuId());
        
        // 根据 SKU 中的 goodsId 获取商品信息
        return goodsService.getById(sku.getGoodsId());
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