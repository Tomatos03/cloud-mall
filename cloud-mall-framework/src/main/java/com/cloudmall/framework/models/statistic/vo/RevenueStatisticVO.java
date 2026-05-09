package com.cloudmall.framework.models.statistic.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 营收统计视图对象
 *
 * @author Tomatos
 * @date 2025/12/24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueStatisticVO {

    /**
     * 总营收额（单位：分）
     */
    private Long totalRevenue;

    /**
     * 订单数量
     */
    private Long orderCount;

    /**
     * 时间周期（天数：7 或 30）
     */
    private Integer days;
}