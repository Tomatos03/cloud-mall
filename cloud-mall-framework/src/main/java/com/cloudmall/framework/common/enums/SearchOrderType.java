package com.cloudmall.framework.common.enums;

import com.cloudmall.framework.exception.BizException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 商品搜索排序类型枚举
 *
 * @author Tomatos
 * @date 2025/12/22
 */
@Getter
@AllArgsConstructor
public enum SearchOrderType {
    /**
     * 综合
     */
    COMPREHENSIVE("default"),
    /**
     * 销量
     */
    SALES("sales"),
    /**
     * 新品
     */
    NEWEST("new");

    /**
     * 排序类型代码
     */
    private final String code;

    public static SearchOrderType of(String code) {
        return Arrays.stream(values())
                .filter(type -> type.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new BizException(BizErrorCode.UNKNOWN_SEARCH_ORDER_TYPE));
    }
}