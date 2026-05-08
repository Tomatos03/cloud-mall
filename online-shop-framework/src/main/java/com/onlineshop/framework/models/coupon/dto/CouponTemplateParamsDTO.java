package com.onlineshop.framework.models.coupon.dto;

import com.onlineshop.framework.common.entity.PageParamsDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CouponTemplateParamsDTO extends PageParamsDTO {

    private Long storeId;
    private Integer status;
    private Integer type;
}
