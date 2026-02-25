package com.onlineshop.framework.models.seckill.service;

import java.util.Map;

/**
 * 秒杀活动统计服务接口
 * 负责计算秒杀活动的各种统计数据
 *
 * @author Tomatos
 * @date 2026/2/26
 */
public interface SeckillActivityStatsService {
    
    /**
     * 获取商家在指定活动中的数据统计
     * 包括：总商品数、总库存、已售数量、订单数、转化率、总销售额等
     * 
     * @param activityId 活动ID
     * @param merchantId 商家ID
     * @return 包含各种统计数据的Map
     */
    Map<String, Object> getMerchantActivityStats(Long activityId, Long merchantId);
}
