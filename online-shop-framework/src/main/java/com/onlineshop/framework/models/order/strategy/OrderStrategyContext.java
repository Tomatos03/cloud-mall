package com.onlineshop.framework.models.order.strategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.models.cart.CartType;
import com.onlineshop.framework.utils.AssertUtils;

/**
 * 订单策略上下文
 * 职责：管理并提供订单相关策略
 *
 * @author : Tomatos
 * @date : 2025/12/24
 */
@Component
@RequiredArgsConstructor
public class OrderStrategyContext {

    private final List<OrderBuildStrategy> buildStrategies;
    private final List<OrderValidateStrategy> validateStrategies;

    private final Map<String, OrderBuildStrategy> buildStrategyMap = new HashMap<>();
    private final Map<String, OrderValidateStrategy> validateStrategyMap = new HashMap<>();

    /**
     * 初始化策略映射
     */
    @PostConstruct
    public void initStrategyMap() {
        for (OrderBuildStrategy strategy : buildStrategies) {
            buildStrategyMap.put(strategy.getSupportedCartType().name(), strategy);
        }
        for (OrderValidateStrategy strategy : validateStrategies) {
            validateStrategyMap.put(strategy.getSupportCartType().name(), strategy);
        }
    }

    /**
     * 获取订单构建策略
     *
     * @param cartType 购物车类型
     * @return 订单构建策略
     */
    public OrderBuildStrategy getBuildStrategy(CartType cartType) {
        AssertUtils.notNull(cartType, BizErrorCode.UNKNOW_CART_TYPE);
        OrderBuildStrategy strategy = buildStrategyMap.get(cartType.name());
        AssertUtils.notNull(strategy, BizErrorCode.UNKNOW_CART_TYPE);
        return strategy;
    }

    /**
     * 获取订单校验策略
     *
     * @param cartType 购物车类型
     * @return 订单校验策略
     */
    public OrderValidateStrategy getValidateStrategy(CartType cartType) {
        AssertUtils.notNull(cartType, BizErrorCode.UNKNOW_CART_TYPE);
        OrderValidateStrategy strategy = validateStrategyMap.get(cartType.name());
        AssertUtils.notNull(strategy, BizErrorCode.UNKNOW_CART_TYPE);
        return strategy;
    }
}
