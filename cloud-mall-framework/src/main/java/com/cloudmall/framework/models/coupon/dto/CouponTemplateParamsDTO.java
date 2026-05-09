package com.cloudmall.framework.models.coupon.dto;

import com.cloudmall.framework.common.entity.PageParamsDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CouponTemplateParamsDTO extends PageParamsDTO {

    private Long storeId;
    private Integer status;
    private Integer type;
}
