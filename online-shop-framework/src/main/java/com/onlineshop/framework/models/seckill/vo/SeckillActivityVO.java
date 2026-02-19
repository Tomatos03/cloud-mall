package com.onlineshop.framework.models.seckill.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀活动视图对象
 */
@Data
public class SeckillActivityVO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 秒杀开始时间
     */
    private LocalDateTime startTime;

    /**
     * 秒杀结束时间
     */
    private LocalDateTime endTime;

    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;

    /**
     * 秒杀库存
     */
    private Integer stock;

    /**
     * 剩余库存
     */
    private Integer remainingStock;

    /**
     * 秒杀状态：0-未开始 1-进行中 2-已结束
     */
    private Integer status;
}