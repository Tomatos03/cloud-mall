package com.onlineshop.framework.models.seckill.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.onlineshop.framework.models.seckill.entity.SeckillActivity;
import com.onlineshop.framework.models.seckill.entity.SeckillOrder;
import com.onlineshop.framework.models.seckill.enums.SeckillStatusEnum;
import com.onlineshop.framework.models.seckill.exception.InsufficientStockException;
import com.onlineshop.framework.models.seckill.exception.SeckillEndedException;
import com.onlineshop.framework.models.seckill.exception.SeckillNotStartedException;
import com.onlineshop.framework.models.seckill.exception.SeckillRateLimitException;
import com.onlineshop.framework.models.seckill.service.SeckillActivityService;
import com.onlineshop.framework.models.seckill.service.SeckillOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀管理器 - 核心业务逻辑处理
 * 负责秒杀活动的库存管理、限流、订单生成等核心逻辑
 */
@Slf4j
@Component
public class SeckillManager {

    private static final String SECKILL_STOCK_KEY_PREFIX = "seckill:stock:";
    private static final String SECKILL_RATE_LIMIT_KEY_PREFIX = "seckill:rate_limit:";
    private static final String SECKILL_USER_LOCK_KEY_PREFIX = "seckill:user_lock:";
    private static final Integer RATE_LIMIT_COUNT = 10; // 每分钟最多请求10次
    private static final Long RATE_LIMIT_PERIOD = 60L; // 限流时间窗口：60秒

    @Autowired
    private SeckillActivityService seckillActivityService;

    @Autowired
    private SeckillOrderService seckillOrderService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 检查秒杀活动是否有效
     * 
     * @param seckillId 秒杀活动ID
     * @return 秒杀活动状态
     */
    public Integer checkSeckillStatus(Long seckillId) {
        SeckillActivity activity = seckillActivityService.getById(seckillId);
        if (activity == null) {
            throw new IllegalArgumentException("秒杀活动不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartTime())) {
            return SeckillStatusEnum.NOT_STARTED.getCode();
        } else if (now.isAfter(activity.getEndTime())) {
            return SeckillStatusEnum.ENDED.getCode();
        }
        return SeckillStatusEnum.ONGOING.getCode();
    }

    /**
     * 执行秒杀操作 - 原子性库存扣减
     * 
     * @param seckillId 秒杀活动ID
     * @param userId 用户ID
     * @param quantity 购买数量
     * @return 秒杀订单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long participateSeckill(Long seckillId, Long userId, Integer quantity) {
        log.info("用户 {} 参与秒杀活动 {}，购买数量：{}", userId, seckillId, quantity);

        // 1. 限流检查
        checkRateLimit(userId);

        // 2. 活动状态检查
        Integer status = checkSeckillStatus(seckillId);
        if (status.equals(SeckillStatusEnum.NOT_STARTED.getCode())) {
            throw new SeckillNotStartedException("秒杀活动未开始");
        } else if (status.equals(SeckillStatusEnum.ENDED.getCode())) {
            throw new SeckillEndedException("秒杀活动已结束");
        }

        // 3. 获取秒杀活动信息
        SeckillActivity activity = seckillActivityService.getById(seckillId);
        if (activity == null) {
            throw new IllegalArgumentException("秒杀活动不存在");
        }

        // 4. Redis中扣减库存（原子性操作）
        String stockKey = SECKILL_STOCK_KEY_PREFIX + seckillId;
        Long remainingStock = (Long) redisTemplate.opsForValue().get(stockKey);
        
        if (remainingStock == null) {
            // 初始化库存到Redis
            remainingStock = activity.getStock().longValue();
            redisTemplate.opsForValue().set(stockKey, remainingStock);
        }

        if (remainingStock < quantity) {
            throw new InsufficientStockException("秒杀商品库存不足");
        }

        // 原子性扣减库存
        Long deductedStock = redisTemplate.opsForValue().decrement(stockKey, quantity);
        if (deductedStock < 0) {
            // 回滚
            redisTemplate.opsForValue().increment(stockKey, quantity);
            throw new InsufficientStockException("秒杀商品库存不足");
        }

        // 5. 生成秒杀订单
        SeckillOrder order = SeckillOrder.builder()
                .seckillId(seckillId)
                .productId(activity.getProductId())
                .userId(userId)
                .seckillPrice(activity.getSeckillPrice())
                .quantity(quantity)
                .status(0) // 待支付
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        seckillOrderService.save(order);
        log.info("秒杀订单创建成功，订单ID：{}", order.getId());

        return order.getId();
    }

    /**
     * 用户限流检查 - 防止刷单
     * 
     * @param userId 用户ID
     */
    private void checkRateLimit(Long userId) {
        String rateLimitKey = SECKILL_RATE_LIMIT_KEY_PREFIX + userId;
        Long count = redisTemplate.opsForValue().increment(rateLimitKey);
        
        if (count == 1) {
            // 第一次请求，设置过期时间
            redisTemplate.expire(rateLimitKey, RATE_LIMIT_PERIOD, TimeUnit.SECONDS);
        }
        
        if (count > RATE_LIMIT_COUNT) {
            throw new SeckillRateLimitException("请求过于频繁，请稍后再试");
        }
    }

    /**
     * 同步Redis库存到数据库
     * 用于秒杀结束后进行库存最终确认
     * 
     * @param seckillId 秒杀活动ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void syncStockToDatabase(Long seckillId) {
        log.info("开始同步秒杀活动 {} 的库存到数据库", seckillId);
        
        String stockKey = SECKILL_STOCK_KEY_PREFIX + seckillId;
        Long remainingStock = (Long) redisTemplate.opsForValue().get(stockKey);
        
        if (remainingStock != null) {
            SeckillActivity activity = seckillActivityService.getById(seckillId);
            activity.setStock(remainingStock.intValue());
            seckillActivityService.updateById(activity);
            
            // 清除Redis缓存
            redisTemplate.delete(stockKey);
            log.info("秒杀活动 {} 库存已同步到数据库，剩余库存：{}", seckillId, remainingStock);
        }
    }

    /**
     * 初始化秒杀活动的Redis库存
     * 
     * @param seckillId 秒杀活动ID
     */
    public void initializeStock(Long seckillId) {
        SeckillActivity activity = seckillActivityService.getById(seckillId);
        if (activity == null) {
            throw new IllegalArgumentException("秒杀活动不存在");
        }

        String stockKey = SECKILL_STOCK_KEY_PREFIX + seckillId;
        redisTemplate.opsForValue().set(stockKey, activity.getStock().longValue());
        log.info("秒杀活动 {} 库存已初始化到Redis，初始库存：{}", seckillId, activity.getStock());
    }

    /**
     * 获取秒杀活动的剩余库存
     * 
     * @param seckillId 秒杀活动ID
     * @return 剩余库存
     */
    public Long getRemainingStock(Long seckillId) {
        String stockKey = SECKILL_STOCK_KEY_PREFIX + seckillId;
        Object stock = redisTemplate.opsForValue().get(stockKey);
        
        if (stock == null) {
            // Redis中没有缓存，从数据库加载
            SeckillActivity activity = seckillActivityService.getById(seckillId);
            if (activity == null) {
                return 0L;
            }
            return activity.getStock().longValue();
        }
        
        return (Long) stock;
    }

    /**
     * 清除秒杀活动的所有缓存
     * 
     * @param seckillId 秒杀活动ID
     */
    public void clearSeckillCache(Long seckillId) {
        String stockKey = SECKILL_STOCK_KEY_PREFIX + seckillId;
        redisTemplate.delete(stockKey);
        log.info("秒杀活动 {} 的缓存已清除", seckillId);
    }
}