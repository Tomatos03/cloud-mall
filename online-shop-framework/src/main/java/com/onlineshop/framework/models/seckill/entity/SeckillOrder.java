package com.onlineshop.framework.models.seckill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀订单表
 */
@Data
@TableName("seckill_order")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillOrder implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 主键，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 秒杀商品ID（外键关联seckill_goods表）
     */
    private Long seckillGoodsId;

    /**
     * 秒杀活动ID（冗余存储，可通过seckillGoodsId关联获取）
     */
    private Long activityId;

    /**
     * 商品ID（冗余存储）
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
     * 秒杀订单状态：CREATED-待支付 PAID-待发货 SHIPPED-待收货 FINISHED-已完成 CANCELED-已取消 CLOSED-已关闭
     */
    private String status;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}