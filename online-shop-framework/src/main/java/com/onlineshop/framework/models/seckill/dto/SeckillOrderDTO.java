package com.onlineshop.framework.models.seckill.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 秒杀订单数据传输对象
 */
@Data
public class SeckillOrderDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 秒杀活动ID
     */
    private Long seckillActivityId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 购买数量
     */
    private Integer quantity;

    /**
     * 收货地址
     */
    private String address;

    /**
     * 收货电话
     */
    private String phone;

    /**
     * 收货人名称
     */
    private String userName;
}