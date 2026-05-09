package com.cloudmall.framework.models.order.enums;

import com.cloudmall.framework.common.enums.BizErrorCode;
import com.cloudmall.framework.exception.BizException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * 订单状态枚举
 *
 * @author : Tomatos
 * @date : 2025/12/21
 */
@Getter
@AllArgsConstructor
public enum OrderStatus {
    CREATED("CREATED", "待支付"),
    PAID("PAID", "待发货"),
    SHIPPED("SHIPPED", "待收货"),
    FINISHED("FINISHED", "已完成"),
    CANCELED("CANCELED", "已取消"),
    CLOSED("CLOSED", "已关闭");

    private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = new EnumMap<>(OrderStatus.class);

    static {
        TRANSITIONS.put(CREATED, Set.of(PAID, CANCELED, CLOSED));
        TRANSITIONS.put(PAID, Set.of(SHIPPED, CANCELED, CLOSED));
        TRANSITIONS.put(SHIPPED, Set.of(FINISHED, CANCELED, CLOSED));
        TRANSITIONS.put(FINISHED, Set.of(CLOSED));
        TRANSITIONS.put(CANCELED, Set.of(CLOSED));
        TRANSITIONS.put(CLOSED, Set.of());
    }

    private final String code;
    private final String desc;

    public boolean canTransferTo(OrderStatus target) {
        return TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }

    public static OrderStatus of(String code) {
        return Arrays.stream(values())
                     .filter(orderStatus -> orderStatus.code.equals(code))
                     .findFirst()
                     .orElseThrow(() -> new BizException(BizErrorCode.INVALID_ORDER_STATUS));
    }
}