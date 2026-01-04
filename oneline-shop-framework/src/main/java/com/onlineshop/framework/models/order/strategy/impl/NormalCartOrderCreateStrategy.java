package com.onlineshop.framework.models.order.strategy.impl;

import com.onlineshop.framework.models.order.dto.TradeShopDTO;
import com.onlineshop.framework.models.order.dto.TradeShopItemDTO;
import com.onlineshop.framework.models.goods.Goods;
import com.onlineshop.framework.models.cart.CartType;
import com.onlineshop.framework.models.order.enums.OrderStatus;
import com.onlineshop.framework.models.goods.IGoodsService;
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
 * 批量查询优化（方案二）：
 * 在 beforeBuildOrderItems() 钩子方法中一次性加载店铺的所有商品
 * 然后在 getGoods() 中从缓存中查询，避免重复数据库查询
 * 在 afterBuildOrderItems() 钩子方法中清理缓存，防止不同请求的数据污染
 *
 * @author : Tomatos
 * @date : 2025/12/24
 */
@Slf4j
@Component
public class NormalCartOrderCreateStrategy extends AbstractOrderCreateStrategy {
    private Map<Long, Goods> currentStoreGoodsMap;

    public NormalCartOrderCreateStrategy(IGoodsService goodsService) {
        super(goodsService);
    }

    @Override
    protected void beforeBuildOrderItems(TradeShopDTO shopDTO) {
        Long storeId = shopDTO.getStoreId();
        
        // 一次性加载该店铺的所有可用商品（单次 DB 查询）
        this.currentStoreGoodsMap = goodsService.getAvailableGoodsByStoreId(storeId)
                                               .stream()
                                               .collect(
                                                       Collectors.toMap(
                                                               Goods::getId,
                                                               goods -> goods,
                                                               // 如果有重复 key（正常不会），保留第一个
                                                               (existing, replacement) -> existing
                                                       )
                                               );
        
        log.debug("预加载店铺 {} 的商品完成，商品总数: {}", storeId, currentStoreGoodsMap.size());
    }

    @Override
    protected Goods getGoods(TradeShopDTO shopDTO, TradeShopItemDTO itemDTO) {
        // 从缓存中查询商品
        return currentStoreGoodsMap.get(itemDTO.getGoodsId());
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
        this.currentStoreGoodsMap = null;
        
        log.debug("清理店铺 {} 的商品缓存完成", shopDTO.getStoreId());
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