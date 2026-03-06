package com.onlineshop.framework.models.seckill.dto;

import com.onlineshop.framework.common.entity.PageParamsDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 秒杀商品查询参数DTO
 * 用于分页查询秒杀商品，支持活动ID等筛选条件
 *
 * @author Tomatos
 * @date 2026/3/6
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SeckillGoodsParamsDTO extends PageParamsDTO {

    /**
     * 秒杀活动ID（必需）
     */
    private Long activityId;

    /**
     * 商家ID（可选，用于查询特定商家的商品）
     */
    private Long merchantId;
}
