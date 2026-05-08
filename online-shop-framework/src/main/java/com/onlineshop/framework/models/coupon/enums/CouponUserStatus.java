package com.onlineshop.framework.models.coupon.enums;

import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.exception.BizException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

@Getter
@AllArgsConstructor
public enum CouponUserStatus {
    UNUSED(1, "未使用"),
    LOCKED(2, "锁定"),
    USED(3, "已使用"),
    EXPIRED(4, "已过期");

    private static final Map<CouponUserStatus, Set<CouponUserStatus>> TRANSITIONS = new EnumMap<>(CouponUserStatus.class);

    static {
        TRANSITIONS.put(UNUSED, Set.of(LOCKED));
        TRANSITIONS.put(LOCKED, Set.of(USED, UNUSED));
    }

    private final int code;
    private final String desc;

    public boolean canTransferTo(CouponUserStatus target) {
        return TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }

    public void validateTransferTo(CouponUserStatus target) {
        if (!canTransferTo(target)) {
            throw new BizException(BizErrorCode.COUPON_NOT_AVAILABLE);
        }
    }

    public static CouponUserStatus of(int code) {
        return Arrays.stream(values())
                     .filter(item -> item.code == code)
                     .findFirst()
                     .orElseThrow(() -> new BizException(BizErrorCode.COUPON_NOT_AVAILABLE));
    }
}
