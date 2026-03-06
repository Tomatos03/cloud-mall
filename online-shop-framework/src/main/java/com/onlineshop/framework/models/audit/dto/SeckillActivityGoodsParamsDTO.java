package com.onlineshop.framework.models.audit.dto;

import com.onlineshop.framework.common.entity.PageParamsDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 秒杀活动商品查询参数DTO
 * 用于分页查询秒杀活动中的商品（审核通过和待审核）
 *
 * 继承 PageParamsDTO，获得 page 和 pageSize 的默认值。
 * 添加 activityId 作为查询条件。
 *
 * @author Tomatos
 * @date 2026/3/6
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SeckillActivityGoodsParamsDTO extends PageParamsDTO {

    /**
     * 秒杀活动ID（必需）
     */
    private Long activityId;
}
