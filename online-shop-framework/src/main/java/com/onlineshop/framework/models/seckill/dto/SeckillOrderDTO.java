package com.onlineshop.framework.models.seckill.dto;

import java.io.Serializable;

import lombok.Data;

/**
 * 秒杀订单数据传输对象
 */
@Data
public class SeckillOrderDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private Long goodsId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 购买数量
     */
    private Integer quantity;

}
