package com.onlineshop.framework.models.coupon.models;

import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.exception.BizException;
import com.onlineshop.framework.models.coupon.enums.CouponUserStatus;

public final class CouponStatusMachine {

    private CouponStatusMachine() {
    }

    public static void validateLock(CouponUserStatus current) {
        if (current != CouponUserStatus.UNUSED) {
            throw new BizException(BizErrorCode.COUPON_NOT_AVAILABLE);
        }
    }

    public static void validateUse(CouponUserStatus current) {
        if (current != CouponUserStatus.LOCKED) {
            throw new BizException(BizErrorCode.COUPON_NOT_AVAILABLE);
        }
    }

    public static void validateRelease(CouponUserStatus current) {
        if (current != CouponUserStatus.LOCKED) {
            throw new BizException(BizErrorCode.COUPON_NOT_AVAILABLE);
        }
    }
}
