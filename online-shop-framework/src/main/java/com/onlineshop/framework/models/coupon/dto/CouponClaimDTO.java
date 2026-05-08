package com.onlineshop.framework.models.coupon.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CouponClaimDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long templateId;
}
