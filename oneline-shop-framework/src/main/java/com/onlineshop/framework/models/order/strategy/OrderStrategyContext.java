package com.onlineshop.framework.models.order.strategy;

import com.onlineshop.framework.models.cart.CartType;
import com.onlineshop.framework.models.order.dto.TradeDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单策略上下文
 * 职责：管理和调用订单相关的策略
 *
 * @author : Tomatos
 * @date : 2025/12/24
 */
@Component
@RequiredArgsConstructor
public class OrderStrategyContext {

    private final List<OrderCreateStrategy> createStrategies;
    private final List<OrderValidateStrategy> validateStrategies;

    private static Map<String, OrderCreateStrategy> createStrategyMap;
    private static Map<String, OrderValidateStrategy> validateStrategyMap;
    /**
     * 初始化策略映射
     */
    private static volatile boolean initialized = false;

    /**
     * 校验订单
     *
     * @param cartType 购物车类型
     * @param tradeDTO 交易数据
     */
    public void validate(CartType cartType, TradeDTO tradeDTO) {
        OrderValidateStrategy strategy = getValidateStrategy(cartType);
        if (strategy != null) {
            strategy.validate(tradeDTO);
        }
    }

    /**
     * 获取订单校验策略
     *
     * @param cartType 购物车类型
     * @return 订单校验策略
     */
    private OrderValidateStrategy getValidateStrategy(CartType cartType) {
        ensureInit();
        return validateStrategyMap.get(cartType.name());
    }

    private void ensureInit() {
        if (initialized) return;
        synchronized (OrderStrategyContext.class) {
            if (initialized) return;
            createStrategyMap = new HashMap<>();
            validateStrategyMap = new HashMap<>();

            for (OrderCreateStrategy strategy : createStrategies) {
                createStrategyMap.put(
                        strategy.getSupportedCartType()
                                .name(),
                        strategy
                );
            }

            for (OrderValidateStrategy strategy : validateStrategies) {
                validateStrategyMap.put(
                        strategy.getSupportCartType()
                                .name(),
                        strategy
                );
            }

            initialized = true;
        }
    }

    /**
     * 构建订单对象和订单明细
     * 注意：此方法只构建订单对象和订单明细，不保存到数据库
     *
     * @param cartType 购物车类型
     * @param tradeDTO 交易数据
     * @return 构建好的订单结果列表（按店铺分组）
     */
    public OrderCreateStrategy.OrderBuildResult buildOrders(CartType cartType, TradeDTO tradeDTO) {
        OrderCreateStrategy strategy = getCreateStrategy(cartType);
        if (strategy != null) {
            return strategy.buildOrders(tradeDTO);
        }
        return null;
    }

    /**
     * 获取订单创建策略
     *
     * @param cartType 购物车类型
     * @return 订单创建策略
     */
    private OrderCreateStrategy getCreateStrategy(CartType cartType) {
        ensureInit();
        return createStrategyMap.get(cartType.name());
    }
}