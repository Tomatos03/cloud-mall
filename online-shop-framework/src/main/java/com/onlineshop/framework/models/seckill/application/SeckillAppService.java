package com.onlineshop.framework.models.seckill.application;

import com.onlineshop.framework.models.seckill.application.vo.SeckillParticipateResultVO;

/**
 * 秒杀应用服务
 * 协调各个service，完成完整的秒杀业务流程
 * 负责事务边界和流程编排
 */
public interface SeckillAppService {
    
    /**
     * 参与秒杀（完整业务流程）
     * 用户购买指定秒杀商品，协调限流、库存检查、订单生成等操作
     *
     * @param seckillGoodsId 秒杀商品ID
     * @param userId 用户ID
     * @param quantity 购买数量
     * @return 秒杀参与结果
     */
    SeckillParticipateResultVO participateSeckill(Long seckillGoodsId, Long userId, Integer quantity);
    
    /**
     * 启动秒杀活动（完整启动流程）
     * 检查活动状态，批量初始化所有关联秒杀商品的库存到Redis
     *
     * @param activityId 秒杀活动ID
     * @return 是否启动成功
     */
    boolean startSeckillActivity(Long activityId);
    
    /**
     * 同步单个秒杀商品的库存到数据库
     * 从Redis读取库存，更新到SeckillGoods表
     *
     * @param seckillGoodsId 秒杀商品ID
     * @return 是否同步成功
     */
    boolean syncStockToDatabase(Long seckillGoodsId);
}