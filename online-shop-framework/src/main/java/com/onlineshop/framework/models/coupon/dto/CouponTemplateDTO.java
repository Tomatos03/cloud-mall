package com.onlineshop.framework.models.coupon.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CouponTemplateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private Integer type;
    private Long storeId;
    private Long discountAmount;
    private Integer discountRate;
    private Long maxDiscountAmount;
    private Long minOrderAmount;
    private Integer scopeType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalCount;
    private Integer perUserLimit;
    private List<Long> scopeRefIds;
}
