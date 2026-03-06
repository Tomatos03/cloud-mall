package com.onlineshop.framework.models.seckill.dto;

import com.onlineshop.framework.common.entity.PageParamsDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 秒杀活动查询参数DTO
 * 用于分页查询秒杀活动，支持多端（管理端、商家端、用户端）
 *
 * @author Tomatos
 * @date 2026/3/6
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SeckillActivityParamsDTO extends PageParamsDTO {
    // 可扩展：后续可添加筛选条件，如状态、日期范围等
}
