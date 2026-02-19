package com.onlineshop.framework.models.seckill.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀订单视图对象
 */
@Data
public class SeckillOrderVO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 秒杀订单ID
     */
    private Long id;

    /**
     * 秒杀活动ID
     */
    private Long seckillId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;

    /**
     * 购买数量
     */
    private Integer quantity;

    /**
     * 订单总金额（秒杀价格 * 数量）
     */
    private BigDecimal totalAmount;

    /**
     * 秒杀订单状态：0-待支付 1-已支付 2-已发货 3-已完成 4-已取消 5-已退货
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}