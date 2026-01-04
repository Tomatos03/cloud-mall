package com.onlineshop.controller.merchant;

import com.onlineshop.framework.models.statistic.IStatisticService;
import com.onlineshop.framework.models.statistic.vo.CategorySalesRatioVO;
import com.onlineshop.framework.models.statistic.vo.DashboardOverviewVO;
import com.onlineshop.framework.models.statistic.vo.FavoriteGoodsTopVO;
import com.onlineshop.framework.models.statistic.vo.FavoriteOverviewVO;
import com.onlineshop.framework.models.statistic.vo.GoodsSalesTopVO;
import com.onlineshop.framework.models.statistic.vo.RevenueVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商家统计管理 Controller
 * 商家只能查看自己店铺的统计数据
 *
 * @author Tomatos
 * @date 2025/12/24
 */
@Slf4j
@RestController
@RequestMapping("/manager/merchant/statistics")
public class MerchantStatisticController {

    @Autowired
    private IStatisticService statisticService;

    /**
     * 查询自己店铺的营收趋势（商家权限）
     * 商家只能查询自己店铺的营收趋势
     * 返回指定天数内每日的营收和订单数
     *
     * @param days 查询天数（如：7、30、90等）
     * @return 每日营收趋势列表
     */
    @GetMapping("/revenue-trend")
    public List<RevenueVO> queryRevenueByDaysTrend(@RequestParam int days) {
        log.debug("商家查询{}天的营收趋势数据", days);
        return statisticService.queryRevenueByDaysTrendMerchant(days);
    }

    /**
     * 查询自己店铺的商品销售排行（商家权限）
     * 商家只能查询自己店铺的销售排行
     *
     * @param top 查询数量，默认为10
     * @return 商品销售排行列表
     */
    @GetMapping("/goods/sales-rank")
    public List<GoodsSalesTopVO> queryGoodsSalesRank(@RequestParam(value = "top", defaultValue = "10") int top) {
        log.debug("商家查询商品销售排行，top: {}", top);
        return statisticService.queryGoodsSalesRankMerchant(top);
    }

    /**
     * 查询自己店铺的商品收藏排行（商家权限）
     * 商家只能查询自己店铺的收藏排行
     *
     * @param top 查询数量，默认为10
     * @return 商品收藏排行列表
     */
    @GetMapping("/goods/favorite-rank")
    public List<FavoriteGoodsTopVO> queryGoodsFavoriteRank(@RequestParam(value = "top", defaultValue = "10") int top) {
        log.debug("商家查询商品收藏排行，top: {}", top);
        return statisticService.queryGoodsFavoriteRankMerchant(top);
    }

    /**
     * 查询自己店铺的类目销售占比（商家权限）
     * 商家只能查询自己店铺的类目销售占比
     *
     * @return 类目销售占比列表
     */
    @GetMapping("/category/sales-ratio")
    public List<CategorySalesRatioVO> queryCategorySalesRatio() {
        log.debug("商家查询类目销售占比");
        return statisticService.queryCategorySalesRatioMerchant();
    }

    /**
     * 查询商家仪表板概览信息（商家权限）
     * 商家可以查看自己店铺的关键统计指标
     * 返回：今日营收、今日订单数、今日新增用户数、累计营收
     *
     * @return 仪表板概览信息
     */
    @GetMapping("/dashboard/overview")
    public DashboardOverviewVO queryDashboardOverview() {
        log.debug("商家查询仪表板概览信息");
        return statisticService.queryDashboardOverviewMerchant();
    }

    /**
     * 查询商家收藏概览信息（商家权限）
     * 商家可以查看自己店铺的收藏统计数据
     * 返回：今日新增收藏、今日取消收藏、今日净增加、累计收藏总数
     *
     * @return 收藏概览信息
     */
    @GetMapping("/favorite/overview")
    public FavoriteOverviewVO queryFavoriteOverview() {
        log.debug("商家查询收藏概览信息");
        return statisticService.queryFavoriteOverviewMerchant();
    }
}
