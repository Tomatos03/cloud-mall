package com.cloudmall.framework.models.cart;

import com.cloudmall.framework.common.enums.BizErrorCode;
import com.cloudmall.framework.exception.BizException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 购物车类型枚举
 *
 * @author : Tomatos
 * @date : 2025/12/24
 */
@Getter
@AllArgsConstructor
public enum PurchaseMode {
    /**
     * 普通购物车
     */
    CART_BUY("CART_BUY", "购物车购买"),

    /**
     * 立即购买
     */
    INSTANT_BUY("INSTANT_BUY", "立即购买");

    private final String code;
    private final String description;

    public static PurchaseMode of(String code) {
        return Arrays.stream(values())
                     .filter(cartType -> cartType.code.equals(code))
                     .findFirst()
                     .orElseThrow(() -> new BizException(BizErrorCode.UNKNOW_CART_TYPE));
    }
}