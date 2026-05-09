package com.cloudmall.framework.models.coupon.enums;

import com.cloudmall.framework.common.enums.BizErrorCode;
import com.cloudmall.framework.exception.BizException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum CouponClaimLuaResult {
    STOCK_NOT_ENOUGH(-1, "库存不足"),
    REACH_LIMIT(-2, "已达到领取上限");

    private final int code;
    private final String desc;

    public static CouponClaimLuaResult of(int code) {
        return Arrays.stream(values())
                     .filter(item -> item.code == code)
                     .findFirst()
                     .orElseThrow(() -> new BizException(BizErrorCode.COUPON_CLAIM_FAILED));
    }
}
