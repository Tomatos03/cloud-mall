package com.onlineshop.framework.models.statistic.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 统计数据综合VO
 * 包含所有静态统计数据：仪表板、收藏、销售排行等
 *
 * @author Tomatos
 * @date 2026/02/11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticDataVO {
    /**
     * 仪表板概览信息
     */
    private DashboardOverviewVO dashboardOverview;

    private Map<LocalDate, BigDecimal> revenueTrend;

    /**
     * 商品销售排行（top 10）
     */
    private List<GoodsSalesTopVO> goodsSalesRank;

    /**
     * 商品收藏排行（top 10）
     */
    private List<FavoriteGoodsTopVO> goodsFavoriteRank;

    /**
     * 类目销售占比
     */
    private List<CategorySalesRatioVO> categorySalesRatio;
}

