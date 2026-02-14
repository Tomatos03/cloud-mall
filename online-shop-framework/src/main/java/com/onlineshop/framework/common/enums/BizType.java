package com.onlineshop.framework.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/1/26
 */
@Getter
@AllArgsConstructor
public enum BizType {
    ORDER_TIMEOUT_CLOSE("ORDER_TIMEOUT_CLOSE", "订单超时自动关闭"),
    ;

    private final String code;
    private final String description;
}
