package com.onlineshop.framework.models.coupon.enums;

import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.exception.BizException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum CouponType {
    FIXED_AMOUNT(1, "满减券"),
    PERCENTAGE(2, "折扣券"),
    FREE_SHIPPING(3, "包邮券");

    private final int code;
    private final String desc;

    public static CouponType of(int code) {
        return Arrays.stream(values())
                     .filter(item -> item.code == code)
                     .findFirst()
                     .orElseThrow(() -> new BizException(BizErrorCode.COUPON_TYPE_INVALID));
    }
}
