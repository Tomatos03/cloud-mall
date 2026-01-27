package com.onlineshop.framework.models.order.enums;

import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 订单类型枚举
 *
 * @author : Tomatos
 * @date : 2025/01/24
 */
@Getter
@AllArgsConstructor
public enum OrderType {
    /**
     * 父订单（多店铺场景的聚合订单）
     */
    PARENT("PARENT", "父订单"),

    /**
     * 子订单（多店铺场景下的单个店铺订单）
     */
    SUB("SUB", "子订单"),

    /**
     * 普通订单（单店铺场景）
     */
    NORMAL("NORMAL", "普通订单");

    private final String code;
    private final String description;

    /**
     * 根据 code 获取订单类型
     *
     * @param code 订单类型代码
     * @return 订单类型枚举
     */
    public static OrderType of(String code) {
        return Arrays.stream(values())
                .filter(type -> type.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new BusinessException(BizErrorCode.INVALID_ORDER_TYPE));
    }

    /**
     * 判断是否为父订单
     *
     * @return true-父订单, false-非父订单
     */
    public boolean isParent() {
        return this == PARENT;
    }

    /**
     * 判断是否为子订单
     *
     * @return true-子订单, false-非子订单
     */
    public boolean isSub() {
        return this == SUB;
    }

    /**
     * 判断是否为普通订单
     *
     * @return true-普通订单, false-非普通订单
     */
    public boolean isNormal() {
        return this == NORMAL;
    }
}