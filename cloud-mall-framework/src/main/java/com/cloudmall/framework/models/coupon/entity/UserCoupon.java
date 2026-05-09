package com.cloudmall.framework.models.coupon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("user_coupon")
public class UserCoupon implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long templateId;
    private Long userId;
    private String orderNo;
    private Integer status;
    private LocalDateTime expireTime;
    private LocalDateTime usedTime;
    private LocalDateTime createTime;
}
