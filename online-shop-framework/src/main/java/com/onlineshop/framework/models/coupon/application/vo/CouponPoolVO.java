package com.onlineshop.framework.models.coupon.application.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class CouponPoolVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long templateId;
    private String name;
    private Integer type;
    private Long storeId;
    private String storeName;
    private Long discountAmount;
    private Integer discountRate;
    private Long maxDiscountAmount;
    private Long minOrderAmount;
    private Integer scopeType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalCount;
    private Integer issuedCount;
    private Integer perUserLimit;
    private Integer userClaimedCount;
}
