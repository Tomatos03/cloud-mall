package com.onlineshop.framework.models.audit.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀活动审核请求
 * 包含秒杀活动创建时需要的所有信息
 *
 * @author Tomatos
 * @date 2026/2/26
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SeckillActivityAuditRequest extends AuditRequest {
    
    /**
     * 秒杀活动ID（外键）
     * 商家选择要参与的活动ID
     */
    private Long activityId;
    
    /**
     * 商品ID（外键）
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
}
