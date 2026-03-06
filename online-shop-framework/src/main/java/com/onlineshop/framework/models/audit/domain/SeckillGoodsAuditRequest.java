package com.onlineshop.framework.models.audit.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 秒杀商品申请加入活动的审核请求
 *
 * 设计说明：
 * - 统一采用 List<SeckillGoodsItem> 模型
 * - 无论1个还是多个商品，都放在 items 列表中
 * - 所有商品共享同一个 activityId
 *
 * 数据示例：
 * {
 *   "type": "SECKILL_ACTIVITY",
 *   "applicantId": 1001,
 *   "applicantName": "商家A",
 *   "activityId": 100,
 *   "items": [
 *     {"productId": 1001, "seckillPrice": 99.99, "stock": 100},
 *     {"productId": 1002, "seckillPrice": 49.99, "stock": 200},
 *     {"productId": 1003, "seckillPrice": 199.99, "stock": 50}
 *   ]
 * }
 *
 * @author Tomatos
 * @date 2026/2/26
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SeckillGoodsAuditRequest extends AuditRequest {
    private String type = "SECKILL_ACTIVITY";

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
}