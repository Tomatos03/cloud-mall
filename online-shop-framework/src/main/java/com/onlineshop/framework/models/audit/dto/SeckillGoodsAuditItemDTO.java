package com.onlineshop.framework.models.audit.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 秒杀活动审核项目DTO
 * 代表一个审核批次中的单个秒杀活动审核项目
 * 包含申请加入活动的商品列表
 * 
 * 设计说明：
 * - 对应 AuditItem 表，存储在 snapshot 字段中
 * - 包含秒杀活动和商品的完整信息
 * - 审核员基于此信息做出批准或拒绝决策
 *
 * @author Tomatos
 * @date 2026/3/7
 */
@Data
public class SeckillGoodsAuditItemDTO {
    /**
     * 秒杀活动ID
     * 所有商品都参与这个活动
     */
    private Long activityId;

    /**
     * 商品列表
     * - 必填，至少包含1个商品
     * - 所有商品共享同一个 activityId
     * - 支持N个商品批量申请
     */
    private List<SeckillGoodsItem> items;

    private class SeckillGoodsItem implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 商品ID
         */
        private Long goodsId;

        /**
         * 秒杀价格
         */
        private BigDecimal seckillPrice;

        /**
         * 秒杀库存
         */
        private Integer stock;
    }
}
