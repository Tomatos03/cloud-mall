package com.onlineshop.framework.models.order.state;

import com.onlineshop.framework.enums.BizErrorCode;
import com.onlineshop.framework.exception.BusinessException;
import com.onlineshop.framework.models.order.enums.OrderStatus;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/1/2
 */
public class OrderStatusMachine {
    private static final Map<OrderStatus, Set<OrderStatus>> transitions = Map.ofEntries(
            Map.entry(OrderStatus.CREATED, Set.of(OrderStatus.PAID, OrderStatus.CANCELED, OrderStatus.CLOSED)),
            Map.entry(OrderStatus.PAID, Set.of(OrderStatus.SHIPPED, OrderStatus.CANCELED, OrderStatus.CLOSED)),
            Map.entry(OrderStatus.SHIPPED, Set.of(OrderStatus.FINISHED, OrderStatus.CANCELED, OrderStatus.CLOSED)),
            Map.entry(OrderStatus.FINISHED, Set.of(OrderStatus.CLOSED)),
            Map.entry(OrderStatus.CANCELED, Set.of(OrderStatus.CLOSED)),
            Map.entry(OrderStatus.CLOSED, Set.of())
    );

    /**
     * 校验状态流转是否合法
     */
    public static void validateTransition(OrderStatus from, OrderStatus to) {
        Set<OrderStatus> allowed = transitions.getOrDefault(from, Collections.emptySet());
        if (!allowed.contains(to)) {
            throw new BusinessException(BizErrorCode.INVALID_ORDER_STATUS);
        }
    }
}
