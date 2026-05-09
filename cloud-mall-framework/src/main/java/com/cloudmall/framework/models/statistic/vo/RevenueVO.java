package com.cloudmall.framework.models.statistic.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 营收趋势视图对象
 * 用于展示每日营收数据
 *
 * @author : Tomatos
 * @date : 2025/12/29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueVO {

    /**
     * 日期
     */
    private LocalDate date;

    /**
     * 该日期的营收额（单位：分）
     */
    private Long revenue;
}
