package com.onlineshop.framework.models.statistic;

import com.onlineshop.framework.models.statistic.vo.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 统计服务接口
 *
 * 自动区分用户角色，商家只能查看自己店铺数据，管理员可查看全平台数据
 * 权限控制由实现类内部处理，无需在接口层区分
 *
 * @author Tomatos
 * @date 2025/12/24
 */
public interface IStatisticService {

    /**
     * 查询商品销售排行
     * - 商家：查询自己店铺的销售排行
     * - 管理员：查询所有店铺的销售排行
     *
     * @param top 查询数量，如果为null或小于等于0则默认10
     * @return 商品销售排行列表
     */
    List<GoodsSalesTopVO> queryGoodsSalesRank(Integer top);

    /**
     * 查询商品收藏排行
     * - 商家：查询自己店铺的收藏排行
     * - 管理员：查询所有店铺的收藏排行
     *
     * @param top 查询数量，如果为null或小于等于0则默认10
     * @return 商品收藏排行列表
     */
    List<FavoriteGoodsTopVO> queryGoodsFavoriteRank(Integer top);

    /**
     * 查询每日营收趋势
     * - 商家：查询自己店铺的营收趋势
     * - 管理员：查询全平台的营收趋势
     *
     * @param days 查询天数（例如：7、30）
     * @return 每日营收趋势列表
     */
    Map<LocalDate, BigDecimal> queryLastRevenueTrend(int days);

    /**
     * 查询类目销售占比
     * - 商家：查询自己店铺的类目销售占比
     * - 管理员：查询全平台的类目销售占比
     *
     * @return 类目销售占比列表
     */
    List<CategorySalesRatioVO> queryCategorySalesRatio();

    /**
     * 查询仪表板概览信息
     * - 商家：查询自己店铺的概览信息
     * - 管理员：查询平台的概览信息
     * 包含：今日营收、今日订单数、今日新增用户数、累计营收
     *
     * @return 仪表板概览信息
     */
    DashboardOverviewVO queryDashboardOverview();

    /**
     * 查询统计数据综合信息
     * - 商家：查询自己店铺的所有统计数据
     * - 管理员：查询平台的所有统计数据
     * 包含：仪表板、收藏、销售排行、类目销售占比等
     *
     * @return 统计数据综合信息
     */
    StatisticDataVO queryAllStatisticData();

}