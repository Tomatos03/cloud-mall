package com.onlineshop.framework.models.coupon.enums;

import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.exception.BizException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum CouponScopeType {
    ALL(1, "全场"),
    GOODS(2, "指定商品"),
    CATEGORY(3, "指定分类");

    private final int code;
    private final String desc;

    public static CouponScopeType of(int code) {
        return Arrays.stream(values())
                     .filter(item -> item.code == code)
                     .findFirst()
                     .orElseThrow(() -> new BizException(BizErrorCode.COUPON_NOT_APPLICABLE));
    }
}
