package com.onlineshop.framework.models.statistic.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 *
 *
 * @author : Tomatos
 * @date : 2025/12/29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverviewVO {

    /** 今日营收（已支付订单金额） */
    private BigDecimal todayRevenue;

    /** 今日订单数（成功下单） */
    private Integer todayOrderCount;

    /** 今日新增用户数 */
    private Long todayNewUserCount;

    /** 累计营收（历史总收入） */
    private BigDecimal totalRevenue;
}
