package com.onlineshop.framework.models.coupon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("coupon_usage_log")
public class CouponUsageLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userCouponId;
    private Long userId;
    private String orderNo;
    private Long discountAmount;
    private LocalDateTime createTime;
}
