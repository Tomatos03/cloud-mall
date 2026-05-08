package com.onlineshop.framework.models.coupon.enums;

import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.exception.BizException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum CouponStatus {
    DRAFT(1, "草稿"),
    ACTIVE(2, "生效"),
    PAUSED(3, "暂停"),
    EXPIRED(4, "过期");

    private final int code;
    private final String desc;

    public static CouponStatus of(int code) {
        return Arrays.stream(values())
                     .filter(item -> item.code == code)
                     .findFirst()
                     .orElseThrow(() -> new BizException(BizErrorCode.COUPON_TEMPLATE_NOT_ACTIVE));
    }
}
