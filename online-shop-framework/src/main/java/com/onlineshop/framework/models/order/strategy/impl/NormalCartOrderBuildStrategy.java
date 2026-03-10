package com.onlineshop.framework.models.order.strategy.impl;

import com.onlineshop.framework.models.order.dto.TradeShopDTO;
import com.onlineshop.framework.models.order.dto.TradeShopItemDTO;
import com.onlineshop.framework.models.goods.spu.Goods;
import com.onlineshop.framework.models.goods.sku.GoodsSku;
import com.onlineshop.framework.models.goods.sku.IGoodsSkuService;
import com.onlineshop.framework.models.goods.spec.service.IGoodsSkuSpecService;
import com.onlineshop.framework.models.goods.spec.service.ISpecService;
import com.onlineshop.framework.models.goods.spec.service.ISpecValueService;
import com.onlineshop.framework.models.cart.CartType;
import com.onlineshop.framework.models.order.enums.OrderStatus;
import com.onlineshop.framework.models.goods.spu.IGoodsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 普通购物车订单创建策略
 * 职责：构建订单对象、订单明细、扣减库存、从购物车移除商品
 * 前提：商品和库存校验已在 Validate 策略中完成
 *
 * 批量查询优化：
 * 1. 在 beforeBuildOrderItems() 中一次性加载所有 SKU 信息
 * 2. 从 SKU 中提取商品 ID 并批量加载商品
 * 3. 建立 SKU -> Goods 的映射关系
 * 4. 在 getGoods() 中直接从映射查询，避免重复数据库查询
 * 5. 在 afterBuildOrderItems() 中清理缓存
 *
 * @author : Tomatos
 * @date : 2025/12/24
 */
@Slf4j
@Component
public class NormalCartOrderBuildStrategy extends AbstractOrderBuildStrategy {
    private Map<Long, Goods> skuToGoodsMap;

    public NormalCartOrderBuildStrategy(
            IGoodsService goodsService,
            IGoodsSkuService goodsSkuService,
            IGoodsSkuSpecService goodsSkuSpecService,
            ISpecService specService,
            ISpecValueService specValueService
    ) {
        super(goodsService, goodsSkuService, goodsSkuSpecService, specService, specValueService);
    }

    @Override
    protected void beforeBuildOrderItems(TradeShopDTO shopDTO) {
        List<TradeShopItemDTO> itemList = shopDTO.getTradeShopItemList();

        // 提取所有 SKU ID
        List<Long> skuIds = itemList.stream()
                                    .map(TradeShopItemDTO::getSkuId)
                                    .collect(Collectors.toList());

        if (skuIds.isEmpty()) {
            this.skuToGoodsMap = Map.of();
            return;
        }

        // 一次性批量加载 SKU，并构建 SKU ID -> Goods 的映射
        this.skuToGoodsMap = skuIds.stream()
                                   .distinct()
                                   .map(goodsSkuService::getById)
                                   .collect(Collectors.toMap(
                                           GoodsSku::getId,
                                           sku -> goodsService.getById(sku.getGoodsId()),
                                           (existing, replacement) -> existing
                                   ));

        log.debug("预加载SKU和商品完成，SKU数量: {}", skuToGoodsMap.size());
    }

    @Override
    protected Goods getGoods(TradeShopDTO shopDTO, TradeShopItemDTO itemDTO) {
        // 直接从 SKU -> Goods 映射中查询商品
        return skuToGoodsMap.get(itemDTO.getSkuId());
    }

    /**
     * 钩子方法：在订单明细构建完成后，清理缓存
     *
     * 这是一个重要的清理操作，防止不同请求间的数据污染：
     * - 在高并发场景下，多个请求可能在不同时间点访问这个实例
     * - 如果不清理缓存，后续请求可能获取到前一个请求的缓存数据
     * - 通过在每个请求的最后清理缓存，确保数据隔离
     */
    @Override
    protected void afterBuildOrderItems(TradeShopDTO shopDTO, List<TradeShopItemDTO> itemList) {
        // 清理缓存
        this.skuToGoodsMap = null;
        log.debug("清理SKU和商品缓存完成");
    }

    @Override
    protected String getOrderStatus() {
        return OrderStatus.CREATED.getCode();
    }

    @Override
    public CartType getSupportedCartType() {
        return CartType.NORMAL;
    }
}