package com.onlineshop.framework.models.coupon.application.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponCalcResult {
    private Long userCouponId;
    private Long totalDiscount;
    private Map<Long, Long> itemDiscounts;
}
