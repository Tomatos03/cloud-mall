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
 * 秒杀商品表（关联审核记录）
 * 在秒杀活动申请审核通过后创建，关联审核记录、活动、商品和商家信息
 *
 * @author Tomatos
 * @date 2026/2/26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("seckill_goods")
public class SeckillGoods implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 活动ID（外键关联seckill_activity表）
     */
    private Long activityId;

    /**
     * 审核记录ID（外键关联audit表）
     * 保存商家提交的秒杀申请的审核记录ID
     */
    private Long auditId;

    /**
     * 商品ID（外键关联goods表）
     */
    private Long productId;

    /**
     * 商家ID（外键关联merchant表）
     * 申请秒杀活动的商家ID
     */
    private Long merchantId;

    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;

    /**
     * 秒杀库存
     */
    private Integer stock;

    /**
     * 已售数量
     */
    private Integer soldCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
