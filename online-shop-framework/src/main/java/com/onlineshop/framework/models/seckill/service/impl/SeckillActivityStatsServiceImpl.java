package com.onlineshop.framework.models.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.onlineshop.framework.models.seckill.entity.SeckillGoods;
import com.onlineshop.framework.models.seckill.entity.SeckillOrder;
import com.onlineshop.framework.models.seckill.service.SeckillActivityStatsService;
import com.onlineshop.framework.models.seckill.service.SeckillGoodsService;
import com.onlineshop.framework.models.seckill.service.SeckillOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 秒杀活动统计服务实现
 *
 * @author Tomatos
 * @date 2026/2/26
 */
@Service
@RequiredArgsConstructor
public class SeckillActivityStatsServiceImpl implements SeckillActivityStatsService {
    
    private final SeckillGoodsService seckillGoodsService;
    private final SeckillOrderService seckillOrderService;
    
    @Override
    public Map<String, Object> getMerchantActivityStats(Long activityId, Long merchantId) {
        // 查询当前商家在该活动中的所有秒杀商品
        List<SeckillGoods> goods = seckillGoodsService.list(
                new LambdaQueryWrapper<SeckillGoods>()
                        .eq(SeckillGoods::getActivityId, activityId)
                        .eq(SeckillGoods::getMerchantId, merchantId));

        Map<String, Object> stats = new HashMap<>();
        
        // 如果没有商品，返回空统计数据
        if (goods.isEmpty()) {
            stats.put("totalProducts", 0);
            stats.put("totalStock", 0);
            stats.put("totalSoldCount", 0);
            stats.put("totalOrders", 0);
            stats.put("conversionRate", "0.00%");
            stats.put("totalAmount", BigDecimal.ZERO);
            return stats;
        }

        // 统计商品信息
        int totalProducts = goods.size();
        int totalStock = goods.stream().mapToInt(SeckillGoods::getStock).sum();
        int totalSoldCount = goods.stream().mapToInt(SeckillGoods::getSoldCount).sum();

        // 查询该商家在该活动中的订单
        // 通过商品ID列表查询订单数据
        List<Long> goodsIds = goods.stream().map(SeckillGoods::getId).toList();
        List<SeckillOrder> orders = seckillOrderService.list(
                new LambdaQueryWrapper<SeckillOrder>()
                        .in(SeckillOrder::getProductId, goodsIds));

        // 统计订单信息
        int totalOrders = orders.size();
        BigDecimal totalAmount = orders.stream()
                .map(order -> order.getSeckillPrice().multiply(new BigDecimal(order.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 计算转化率（已售数量 / 总库存）
        double conversionRate = totalStock > 0 ? (double) totalSoldCount / totalStock : 0.0;

        stats.put("totalProducts", totalProducts);
        stats.put("totalStock", totalStock);
        stats.put("totalSoldCount", totalSoldCount);
        stats.put("totalOrders", totalOrders);
        stats.put("conversionRate", String.format("%.2f%%", conversionRate * 100));
        stats.put("totalAmount", totalAmount);

        return stats;
    }
}
