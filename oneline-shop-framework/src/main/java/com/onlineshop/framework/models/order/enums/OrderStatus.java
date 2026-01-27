package com.onlineshop.framework.models.order.enums;

import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 订单状态枚举
 *
 * @author : Tomatos
 * @date : 2025/12/21
 */
@Getter
@AllArgsConstructor
public enum OrderStatus {
    /**
     * 待支付
     */
    CREATED("CREATED", "待支付"),

    /**
     * 待发货
     */
    PAID("PAID", "待发货"),

    /**
     * 待收货
     */
    SHIPPED("SHIPPED", "待收货"),

    /**
     * 已完成
     */
    FINISHED("FINISHED", "已完成"),

    /**
     * 已取消
     */
    CANCELED("CANCELED", "已取消"),

    /**
     * 已关闭
     */
    CLOSED("CLOSED", "已关闭");

    private final String code;
    private final String desc;

    public static OrderStatus of(String code) {
        return Arrays.stream(values())
                     .filter(orderStatus -> orderStatus.code.equals(code))
                     .findFirst()
                     .orElseThrow(() -> new BusinessException(BizErrorCode.INVALID_ORDER_STATUS));
    }
}