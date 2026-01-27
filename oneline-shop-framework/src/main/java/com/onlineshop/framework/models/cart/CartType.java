package com.onlineshop.framework.models.cart;

import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.exception.BusinessException;
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
public enum CartType {
    /**
     * 普通购物车
     */
    NORMAL("NORMAL", "普通购物车"),

    /**
     * 立即购买
     */
    INSTANT_BUY("INSTANT_BUY", "立即购买");

    private final String code;
    private final String description;

    public static CartType of(String code) {
        return Arrays.stream(values())
                     .filter(cartType -> cartType.code.equals(code))
                     .findFirst()
                     .orElseThrow(() -> new BusinessException(BizErrorCode.UNKNOW_CART_TYPE));
    }
}