package com.onlineshop.controller;

import com.onlineshop.framework.models.statistic.IStatisticService;
import com.onlineshop.framework.models.statistic.vo.StatisticDataVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/2/12
 */
@Slf4j
@RestController
@RequestMapping("/merchant/statistics")
@RequiredArgsConstructor
public class StatisticMerchantController {
    private final IStatisticService statisticService;

    /**
     * 获取统计数据综合信息（一次请求获取所有统计数据）
     * 返回：仪表板概览、收藏概览、商品销售排行、商品收藏排行、类目销售占比
     * 管理员权限：查询全平台数据
     * 商家权限：查询自己店铺数据
     *
     * @return 统计数据综合信息
     */
    @GetMapping("/all")
    public StatisticDataVO queryAllStatisticData() {
        return statisticService.queryAllStatisticData();
    }

    /**
     * 查询营收趋势（支持查询不同天数的数据）
     * 管理员权限：查询全平台指定天数的营收额度
     * 商家权限：查询自己店铺的营收趋势
     *
     * @param days 查询天数，例如：7、30、90等，默认7天
     * @return 每日营收趋势列表
     */
    @GetMapping("/revenue-trend")
    public Map<LocalDate, BigDecimal> queryRevenueTrend(@RequestParam(value = "days", defaultValue = "7") int days) {
        return statisticService.queryLastRevenueTrend(days);
    }
}
