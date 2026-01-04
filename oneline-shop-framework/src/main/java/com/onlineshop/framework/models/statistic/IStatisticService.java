package com.onlineshop.framework.models.statistic;

import com.onlineshop.framework.models.statistic.vo.*;

import java.util.List;

/**
 * 统计服务接口
 *
 * @author Tomatos
 * @date 2025/12/24
 */
public interface IStatisticService {

    // ========== 商家方法 ==========

    /**
     * 查询自己店铺的商品销售排行（商家权限）
     *
     * @param top 查询数量，如果为null或小于等于0则默认10
     * @return 商品销售排行列表
     */
    List<GoodsSalesTopVO> queryGoodsSalesRankMerchant(Integer top);

    /**
     * 查询自己店铺的商品收藏排行（商家权限）
     *
     * @param top 查询数量，如果为null或小于等于0则默认10
     * @return 商品收藏排行列表
     */
    List<FavoriteGoodsTopVO> queryGoodsFavoriteRankMerchant(Integer top);

    /**
     * 查询自己店铺指定天数的每日营收趋势（商家权限）
     *
     * @param days 查询天数（例如：7、30）
     * @return 每日营收趋势列表
     */
    List<RevenueVO> queryRevenueByDaysTrendMerchant(int days);

    /**
     * 查询自己店铺的类目销售占比（商家权限）
     * 商家只能查询自己店铺的类目销售占比
     *
     * @return 类目销售占比列表
     */
    List<CategorySalesRatioVO> queryCategorySalesRatioMerchant();

    /**
     * 查询商家仪表板概览信息（商家权限）
     * 包含：今日营收、今日订单数、今日新增用户数、累计营收
     *
     * @return 仪表板概览信息
     */
    DashboardOverviewVO queryDashboardOverviewMerchant();

    /**
     * 查询商家收藏概览信息（商家权限）
     * 包含：今日新增收藏、今日取消收藏、今日净增加、累计收藏总数
     *
     * @return 收藏概览信息
     */
    FavoriteOverviewVO queryFavoriteOverviewMerchant();

    // ========== 管理员方法 ==========

    /**
     * 查询商品销售排行（管理员权限）
     * 管理员可以查询所有店铺的销售排行
     *
     * @param top 查询数量，如果为null或小于等于0则默认10
     * @return 商品销售排行列表
     */
    List<GoodsSalesTopVO> queryGoodsSalesRankAdmin(Integer top);

    /**
     * 查询商品收藏排行（管理员权限）
     * 管理员可以查询所有店铺的收藏排行
     *
     * @param top 查询数量，如果为null或小于等于0则默认10
     * @return 商品收藏排行列表
     */
    List<FavoriteGoodsTopVO> queryGoodsFavoriteRankAdmin(Integer top);



    /**
     * 查询所有店铺指定天数的每日营收趋势（管理员权限）
     *
     * @param days 查询天数（例如：7、30）
     * @return 每日营收趋势列表
     */
    List<RevenueVO> queryRevenueByDaysTrendAdmin(int days);

    /**
     * 查询所有店铺的类目销售占比（管理员权限）
     * 管理员可以查询所有店铺的类目销售占比
     *
     * @return 类目销售占比列表
     */
    List<CategorySalesRatioVO> queryCategorySalesRatioAdmin();

    /**
     * 查询平台仪表板概览信息（管理员权限）
     * 包含：今日营收、今日订单数、今日新增用户数、累计营收
     *
     * @return 仪表板概览信息
     */
    DashboardOverviewVO queryDashboardOverviewAdmin();

    /**
     * 查询平台收藏概览信息（管理员权限）
     * 包含：今日新增收藏、今日取消收藏、今日净增加、累计收藏总数
     *
     * @return 收藏概览信息
     */
    FavoriteOverviewVO queryFavoriteOverviewAdmin();

}