package com.cloudmall.framework.models.statistic;

import com.cloudmall.framework.models.statistic.vo.StatisticDataVO;

import java.math.BigDecimal;
import java.time.LocalDate;
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
     * 查询每日营收趋势
     * - 商家：查询自己店铺的营收趋势
     * - 管理员：查询全平台的营收趋势
     *
     * @param days 查询天数（例如：7、30）
     * @return 每日营收趋势列表
     */
    Map<LocalDate, BigDecimal> queryLastRevenueTrend(int days);

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