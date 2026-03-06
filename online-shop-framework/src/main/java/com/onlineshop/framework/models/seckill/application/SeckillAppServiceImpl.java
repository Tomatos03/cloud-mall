package com.onlineshop.framework.models.seckill.application;

import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.models.order.enums.OrderStatus;
import com.onlineshop.framework.models.seckill.application.vo.SeckillParticipateResultVO;
import com.onlineshop.framework.models.seckill.entity.SeckillActivity;
import com.onlineshop.framework.models.seckill.entity.SeckillGoods;
import com.onlineshop.framework.models.seckill.entity.SeckillOrder;
import com.onlineshop.framework.models.seckill.enums.SeckillActivityStatus;
import com.onlineshop.framework.models.seckill.enums.SeckillStatusEnum;
import com.onlineshop.framework.models.seckill.service.SeckillActivityService;
import com.onlineshop.framework.models.seckill.service.SeckillGoodsService;
import com.onlineshop.framework.models.seckill.service.SeckillOrderService;
import com.onlineshop.framework.utils.AssertUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀应用服务实现
 * 
 * 协调各个service，完成完整的秒杀业务流程：
 * - 用户参与秒杀：按SeckillGoods粒度购买
 * - 活动启动：批量初始化所有秒杀商品库存
 * - 库存同步：从Redis同步到数据库
 * 
 * @author Tomatos
 * @date 2026/2/26
 */
@Service
@Slf4j
public class SeckillAppServiceImpl implements SeckillAppService {
    
    private static final String SECKILL_STOCK_KEY_PREFIX = "seckill:stock:";
    private static final String SECKILL_RATE_LIMIT_KEY_PREFIX = "seckill:rate_limit:";
    private static final Integer RATE_LIMIT_COUNT = 10;
    private static final Long RATE_LIMIT_PERIOD = 60L;
    
    @Autowired
    private SeckillActivityService seckillActivityService;
    @Autowired
    private SeckillGoodsService seckillGoodsService;
    @Autowired
    private SeckillOrderService seckillOrderService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // ==================== 用户参与秒杀 ====================
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SeckillParticipateResultVO participateSeckill(Long seckillGoodsId, Long userId, Integer quantity) {
        log.info("用户 {} 参与秒杀商品 {}，购买数量：{}", userId, seckillGoodsId, quantity);
        
        // 1. 限流检查
        checkRateLimit(userId);
        
        // 2. 活动状态检查
        Integer status = checkSeckillStatus(seckillGoodsId);
        AssertUtils.isFalse(status.equals(SeckillStatusEnum.NOT_STARTED.getCode()), BizErrorCode.SECKILL_NOT_STARTED);
        AssertUtils.isFalse(status.equals(SeckillStatusEnum.ENDED.getCode()), BizErrorCode.SECKILL_ALREADY_ENDED);
        
        // 3. 库存扣减和创建订单
        Long orderId = deductStockAndCreateOrder(seckillGoodsId, userId, quantity);
        
        return SeckillParticipateResultVO.builder()
                .orderId(orderId)
                .success(true)
                .message("秒杀成功")
                .build();
    }
    
    // ==================== 活动启动 ====================
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean startSeckillActivity(Long activityId) {
        log.info("启动秒杀活动，活动ID：{}", activityId);
        
        // 1. 检查活动存在且状态为"报名中"
        SeckillActivity activity = seckillActivityService.getById(activityId);
        AssertUtils.notNull(activity, BizErrorCode.SECKILL_ACTIVITY_NOT_EXIST);
        AssertUtils.isTrue(activity.getStatus() != null && activity.getStatus().equals(SeckillActivityStatus.REGISTRATION.getCode()),
                          BizErrorCode.INVALID_ACTIVITY_STATUS);
        
        // 2. 检查活动时间是否正确（可选，取决于业务需求）
        checkActivityTimeValid(activity);
        
        // 3. 批量初始化所有关联秒杀商品的库存到Redis
        initializeAllGoodsStock(activityId);
        
        // 4. 更新活动状态为"进行中"
        return seckillActivityService.startActivity(activityId);
    }
    
    // ==================== 库存同步 ====================
    
    @Override
    public boolean syncStockToDatabase(Long seckillGoodsId) {
        log.info("同步秒杀商品库存到数据库，商品ID：{}", seckillGoodsId);
        
        // 1. 获取Redis中的库存
        String stockKey = SECKILL_STOCK_KEY_PREFIX + seckillGoodsId;
        Object stockObj = redisTemplate.opsForValue().get(stockKey);
        
        if (stockObj != null) {
            Long remainingStock = Long.parseLong(stockObj.toString());
            
            // 2. 更新SeckillGoods的库存
            SeckillGoods goods = seckillGoodsService.getById(seckillGoodsId);
            if (goods != null) {
                goods.setStock(remainingStock.intValue());
                seckillGoodsService.updateById(goods);
                log.info("秒杀商品库存已同步到数据库，商品ID：{}，剩余库存：{}", seckillGoodsId, remainingStock);
            }
        }
        
        // 3. 清除缓存
        redisTemplate.delete(stockKey);
        log.info("秒杀商品缓存已清除，商品ID：{}", seckillGoodsId);
        
        return true;
    }
    
    // ==================== 私有方法 ====================
    
    /**
     * 检查秒杀活动时间状态
     * 根据关联活动的日期和小时判断当前秒杀是否在进行中
     * 
     * @param seckillGoodsId 秒杀商品ID
     * @return 秒杀状态码（0=未开始，1=进行中，2=已结束）
     */
    private Integer checkSeckillStatus(Long seckillGoodsId) {
        // 1. 获取秒杀商品
        SeckillGoods goods = seckillGoodsService.getById(seckillGoodsId);
        AssertUtils.notNull(goods, BizErrorCode.SECKILL_ACTIVITY_NOT_EXIST);
        
        // 2. 获取关联的活动
        SeckillActivity activity = seckillActivityService.getById(goods.getActivityId());
        AssertUtils.notNull(activity, BizErrorCode.SECKILL_ACTIVITY_NOT_EXIST);
        
        // 3. 计算秒杀活动的开始和结束时间
        // 活动日期 + 开始小时 = 开始时间
        // 活动日期 + (开始小时 + 1) = 结束时间
        LocalDate activityDate = LocalDate.parse(activity.getActivityDate());
        LocalDateTime startTime = activityDate.atTime(LocalTime.of(activity.getStartHour(), 0));
        LocalDateTime endTime = activityDate.atTime(LocalTime.of(activity.getStartHour() + 1, 0));
        
        // 4. 与当前时间比较
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startTime)) {
            return SeckillStatusEnum.NOT_STARTED.getCode();
        } else if (now.isAfter(endTime)) {
            return SeckillStatusEnum.ENDED.getCode();
        }
        return SeckillStatusEnum.ONGOING.getCode();
    }
    
    /**
     * 限流检查
     * 同一用户在60秒内最多发起10次请求
     */
    private void checkRateLimit(Long userId) {
        String rateLimitKey = SECKILL_RATE_LIMIT_KEY_PREFIX + userId;
        Long count = redisTemplate.opsForValue().increment(rateLimitKey);
        
        if (count == 1) {
            redisTemplate.expire(rateLimitKey, RATE_LIMIT_PERIOD, TimeUnit.SECONDS);
        }
        
        AssertUtils.isTrue(count <= RATE_LIMIT_COUNT, BizErrorCode.SECKILL_RATE_LIMIT_EXCEEDED);
    }
    
    /**
     * 扣减库存并创建订单
     * 使用Redis原子操作扣减库存，然后创建秒杀订单
     */
    private Long deductStockAndCreateOrder(Long seckillGoodsId, Long userId, Integer quantity) {
        // 1. 获取秒杀商品信息
        SeckillGoods goods = seckillGoodsService.getById(seckillGoodsId);
        AssertUtils.notNull(goods, BizErrorCode.SECKILL_ACTIVITY_NOT_EXIST);
        
        // 2. 获取或初始化Redis中的库存
        String stockKey = SECKILL_STOCK_KEY_PREFIX + seckillGoodsId;
        Object stockObj = redisTemplate.opsForValue().get(stockKey);
        Long remainingStock;
        
        if (stockObj == null) {
            // 初始化库存到Redis（如果未初始化）
            remainingStock = goods.getStock().longValue();
            redisTemplate.opsForValue().set(stockKey, remainingStock);
            log.info("秒杀商品库存已初始化到Redis，商品ID：{}，库存：{}", seckillGoodsId, remainingStock);
        } else {
            remainingStock = Long.parseLong(stockObj.toString());
        }
        
        // 3. 检查库存是否充足
        AssertUtils.isTrue(remainingStock >= quantity, BizErrorCode.SECKILL_STOCK_INSUFFICIENT);
        
        // 4. 原子性扣减库存
        Long deductedStock = redisTemplate.opsForValue().decrement(stockKey, quantity);
        AssertUtils.isTrue(deductedStock >= 0, BizErrorCode.SECKILL_STOCK_INSUFFICIENT);
        
        // 5. 创建秒杀订单
        SeckillOrder order = SeckillOrder.builder()
                .seckillGoodsId(seckillGoodsId)
                .activityId(goods.getActivityId())
                .productId(goods.getProductId())
                .userId(userId)
                .seckillPrice(goods.getSeckillPrice())
                .quantity(quantity)
                .status(OrderStatus.CREATED.getCode()) // 待支付
                .build();
        
        seckillOrderService.save(order);
        log.info("秒杀订单已创建，订单ID：{}，商品ID：{}，用户ID：{}", order.getId(), seckillGoodsId, userId);
        
        return order.getId();
    }
    
    /**
     * 批量初始化所有秒杀商品的库存
     * 将所有已审核通过的秒杀商品库存加载到Redis
     */
    private void initializeAllGoodsStock(Long activityId) {
        log.info("批量初始化活动的秒杀商品库存，活动ID：{}", activityId);
        
        // 1. 查询该活动的所有秒杀商品
        List<SeckillGoods> allGoods = seckillGoodsService.listByActivityId(activityId);
        
        if (allGoods == null || allGoods.isEmpty()) {
            log.warn("活动中没有秒杀商品，活动ID：{}", activityId);
            return;
        }
        
        // 2. 逐个初始化库存到Redis
        for (SeckillGoods goods : allGoods) {
            String stockKey = SECKILL_STOCK_KEY_PREFIX + goods.getId();
            redisTemplate.opsForValue().set(stockKey, goods.getStock().longValue());
            log.info("秒杀商品库存已初始化到Redis，商品ID：{}，库存：{}", goods.getId(), goods.getStock());
        }
        
        log.info("活动所有秒杀商品库存初始化完成，活动ID：{}，商品数：{}", activityId, allGoods.size());
    }
    
    /**
     * 检查活动时间有效性（可选验证）
     * 确保活动的日期+小时正确对应当前时间范围
     */
    private void checkActivityTimeValid(SeckillActivity activity) {
        LocalDate activityDate = LocalDate.parse(activity.getActivityDate());
        LocalDateTime activityStart = activityDate.atTime(LocalTime.of(activity.getStartHour(), 0));
        LocalDateTime activityEnd = activityDate.atTime(LocalTime.of(activity.getStartHour() + 1, 0));
        
        LocalDateTime now = LocalDateTime.now();
        
        // 活动日期不能是过去的日期
        if (activityDate.isBefore(LocalDate.now())) {
            throw new com.onlineshop.framework.exception.BizException(
                BizErrorCode.SECKILL_ACTIVITY_NOT_EXIST
            );
        }
        
        log.info("活动时间有效，活动ID：{}，开始时间：{}，结束时间：{}", 
                activity.getId(), activityStart, activityEnd);
    }
}
