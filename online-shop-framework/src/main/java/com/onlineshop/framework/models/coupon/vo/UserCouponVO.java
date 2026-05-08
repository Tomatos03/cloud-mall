package com.onlineshop.framework.models.coupon.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class UserCouponVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long templateId;
    private String couponName;
    private Integer couponType;
    private Long discountAmount;
    private Integer discountRate;
    private Long maxDiscountAmount;
    private Long minOrderAmount;
    private Integer status;
    private LocalDateTime expireTime;
    private LocalDateTime usedTime;
}
