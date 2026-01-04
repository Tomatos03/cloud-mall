package com.onlineshop.controller.admin;

import com.onlineshop.framework.models.statistic.IStatisticService;
import com.onlineshop.framework.models.statistic.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员统计管理 Controller
 * 管理员可以查看所有店铺的统计数据
 *
 * @author Tomatos
 * @date 2025/12/24
 */
@Slf4j
@RestController
@RequestMapping("/manager/admin/statistics")
public class AdminStatisticController {

    @Autowired
    private IStatisticService statisticService;

    /**
     * 查询全平台指定天数的营收额度（管理员权限）
     * 管理员可以查询所有店铺的营收
     *
     * @param days 查询天数，例如：7、30、90等
     * @return 营收统计信息
     */
    @GetMapping("/revenue-trend")
    public List<RevenueVO> queryRevenue(@RequestParam(value = "days", defaultValue = "7") int days) {
        log.debug("管理员查询{}天的营收数据", days);
        return statisticService.queryRevenueByDaysTrendAdmin(days);
    }

    /**
     * 查询商品销售排行（管理员权限）
     * 管理员可以查询所有店铺的销售排行
     *
     * @param top 查询数量，默认为10
     * @return 商品销售排行列表
     */
    @GetMapping("/goods/sales-rank")
    public List<GoodsSalesTopVO> queryGoodsSalesRank(@RequestParam(value = "top", defaultValue = "10") int top) {
        log.debug("管理员查询商品销售排行，top: {}", top);
        return statisticService.queryGoodsSalesRankAdmin(top);
    }

    /**
     * 查询商品收藏排行（管理员权限）
     * 管理员可以查询所有店铺的收藏排行
     *
     * @param top 查询数量，默认为10
     * @return 商品收藏排行列表
     */
    @GetMapping("/goods/favorite-rank")
    public List<FavoriteGoodsTopVO> queryGoodsFavoriteRank(@RequestParam(value = "top", defaultValue = "10") int top) {
        log.debug("管理员查询商品收藏排行，top: {}", top);
        return statisticService.queryGoodsFavoriteRankAdmin(top);
    }

    /**
     * 查询全平台类目销售占比（管理员权限）
     * 管理员可以查询所有店铺的类目销售占比
     *
     * @return 类目销售占比列表
     */
    @GetMapping("/category/sales-ratio")
    public List<CategorySalesRatioVO> queryCategorySalesRatio() {
        log.debug("管理员查询全平台类目销售占比");
        return statisticService.queryCategorySalesRatioAdmin();
    }

    /**
     * 查询平台仪表板概览信息（管理员权限）
     * 管理员可以查看全平台的关键统计指标
     * 返回：今日营收、今日订单数、今日新增用户数、累计营收
     *
     * @return 仪表板概览信息
     */
    @GetMapping("/dashboard/overview")
    public DashboardOverviewVO queryDashboardOverview() {
        log.debug("管理员查询平台仪表板概览信息");
        return statisticService.queryDashboardOverviewAdmin();
    }

    @GetMapping("/favorite/overview")
    public FavoriteOverviewVO queryFavoriteOverview() {
        log.debug("管理员查询收藏概览信息");
        return statisticService.queryFavoriteOverviewAdmin();
    }
}