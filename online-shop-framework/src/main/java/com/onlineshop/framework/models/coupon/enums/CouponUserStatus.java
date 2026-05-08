package com.onlineshop.framework.models.coupon.enums;

import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.exception.BizException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum CouponUserStatus {
    UNUSED(1, "未使用"),
    LOCKED(2, "锁定"),
    USED(3, "已使用"),
    EXPIRED(4, "已过期");

    private final int code;
    private final String desc;

    public static CouponUserStatus of(int code) {
        return Arrays.stream(values())
                     .filter(item -> item.code == code)
                     .findFirst()
                     .orElseThrow(() -> new BizException(BizErrorCode.COUPON_NOT_AVAILABLE));
    }
}
