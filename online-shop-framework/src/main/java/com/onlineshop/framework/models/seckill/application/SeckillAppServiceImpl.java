package com.onlineshop.framework.models.seckill.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.models.audit.dto.SeckillGoodsAuditItemDTO;
import com.onlineshop.framework.models.audit.entity.Audit;
import com.onlineshop.framework.models.audit.entity.AuditItem;
import com.onlineshop.framework.models.audit.enums.AuditBizType;
import com.onlineshop.framework.models.audit.enums.AuditStatus;
import com.onlineshop.framework.models.audit.service.IAuditItemService;
import com.onlineshop.framework.models.audit.service.IAuditService;
import com.onlineshop.framework.models.goods.application.IGoodsAppService;
import com.onlineshop.framework.models.goods.application.vo.WebGoodsDetailVO;
import com.onlineshop.framework.models.goods.sku.GoodsSku;
import com.onlineshop.framework.models.goods.sku.IGoodsSkuService;
import com.onlineshop.framework.models.order.enums.OrderStatus;
import com.onlineshop.framework.models.seckill.application.vo.SeckillActivityGoodsPageVO;
import com.onlineshop.framework.models.seckill.application.vo.SeckillGoodsWebDetailVO;
import com.onlineshop.framework.models.seckill.application.vo.SeckillParticipateResultVO;
import com.onlineshop.framework.models.seckill.dto.SeckillGoodsDTO;
import com.onlineshop.framework.models.seckill.dto.SeckillGoodsParamsDTO;
import com.onlineshop.framework.models.seckill.entity.SeckillActivity;
import com.onlineshop.framework.models.seckill.entity.SeckillGoods;
import com.onlineshop.framework.models.seckill.entity.SeckillOrder;
import com.onlineshop.framework.models.seckill.enums.SeckillStatusEnum;
import com.onlineshop.framework.models.seckill.service.SeckillActivityService;
import com.onlineshop.framework.models.seckill.service.SeckillGoodsService;
import com.onlineshop.framework.models.seckill.service.SeckillOrderService;
import com.onlineshop.framework.models.seckill.vo.SeckillActivityVO;
import com.onlineshop.framework.support.JsonSupport;
import com.onlineshop.framework.utils.AssertUtils;

/**
 * 秒杀应用服务实现
 * <p>
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
    private IGoodsSkuService goodsSkuService;
    @Autowired
    private IGoodsAppService goodsAppService;
    @Autowired
    private IAuditService auditService;
    @Autowired
    private IAuditItemService auditItemService;
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

    @Override
    public IPage<SeckillGoodsDTO> pageSeckillActivityGoods(SeckillGoodsParamsDTO params) {
        AssertUtils.notNull(params.getActivityId(), BizErrorCode.ACTIVITY_ID_REQUIRED);
        SeckillActivity activity = seckillActivityService.getById(params.getActivityId());
        AssertUtils.notNull(activity, BizErrorCode.SECKILL_ACTIVITY_NOT_EXIST);

        // 客户端查询（merchantId为空）仍走已通过落库表
        if (params.getMerchantId() == null) {
            Page<SeckillGoods> page = new Page<>(params.getPage(), params.getPageSize());
            LambdaQueryWrapper<SeckillGoods> wrapper = new LambdaQueryWrapper<SeckillGoods>()
                    .eq(SeckillGoods::getActivityId, params.getActivityId())
                    .orderByDesc(SeckillGoods::getCreateTime);
            IPage<SeckillGoods> result = seckillGoodsService.page(page, wrapper);
            return result.convert(this::convertApprovedSeckillGoodsToDTO);
        }

        // 商家查询走审核链路：只看最新版本，避免重复展示
        List<Audit> auditList = auditService.lambdaQuery()
                                            .eq(Audit::getBizType, AuditBizType.SECKILL_GOODS.getCode())
                                            .eq(Audit::getBizPid, params.getActivityId())
                                            .eq(params.getMerchantId() != null, Audit::getApplicantId,
                                                params.getMerchantId())
                                            .ne(Audit::getStatus, AuditStatus.REVOKED.getCode())
                                            .list();
        if (CollUtil.isEmpty(auditList)) {
            return new Page<>(params.getPage(), params.getPageSize(), 0);
        }

        List<Long> auditIdList = auditList.stream()
                                          .map(Audit::getId)
                                          .collect(Collectors.toList());

        LambdaQueryWrapper<AuditItem> wrapper = new LambdaQueryWrapper<AuditItem>()
                .in(AuditItem::getAuditId, auditIdList)
                .eq(AuditItem::getIsLatest, 1)
                .ne(AuditItem::getStatus, AuditStatus.REVOKED.getCode())
                .orderByDesc(AuditItem::getId);

        Page<AuditItem> page = new Page<>(params.getPage(), params.getPageSize());
        IPage<AuditItem> result = auditItemService.page(page, wrapper);
        return result.convert(this::convertAuditItemToDTO);
    }

    @Override
    public SeckillActivityGoodsPageVO pageHourActivityGoods(
            SeckillGoodsParamsDTO params,
            LocalDateTime targetTime
    ) {
        return buildHourActivityGoodsPage(
                params,
                targetTime.toLocalDate().toString(),
                targetTime.getHour()
        );
    }

    @Override
    public List<SeckillActivityGoodsPageVO> listDayActivityGoods(
            SeckillGoodsParamsDTO params,
            LocalDate targetDate
    ) {
        String activityDate = targetDate.toString();
        log.info("查询指定日期全部场次秒杀活动商品，日期：{}，页码：{}，每页大小：{}",
                 activityDate, params.getPage(), params.getPageSize());

        List<SeckillActivity> activityList = seckillActivityService.lambdaQuery()
                                                                   .eq(SeckillActivity::getActivityDate, activityDate)
                                                                   .orderByAsc(SeckillActivity::getStartHour)
                                                                   .orderByDesc(SeckillActivity::getCreateTime)
                                                                   .list();
        if (CollUtil.isEmpty(activityList)) {
            return Collections.emptyList();
        }

        List<SeckillActivityGoodsPageVO> result = new ArrayList<>(activityList.size());
        for (SeckillActivity activity : activityList) {
            SeckillActivityGoodsPageVO seckillActivityGoodsPageVO = buildActivityGoodsPage(params, activity);
            if (seckillActivityGoodsPageVO != null) {
                result.add(seckillActivityGoodsPageVO);
            }
        }
        return result;
    }

    private SeckillActivityGoodsPageVO buildHourActivityGoodsPage(
            SeckillGoodsParamsDTO params,
            String activityDate,
            Integer startHour
    ) {
        log.info("查询整点秒杀活动商品，日期：{}，小时：{}，页码：{}，每页大小：{}",
                 activityDate, startHour, params.getPage(), params.getPageSize());

        SeckillActivity activity = seckillActivityService.lambdaQuery()
                                                         .eq(SeckillActivity::getActivityDate, activityDate)
                                                         .eq(SeckillActivity::getStartHour, startHour)
                                                         .orderByDesc(SeckillActivity::getCreateTime)
                                                         .last("LIMIT 1")
                                                         .one();
        if (activity == null) {
            log.debug("指定时间没有对应的秒杀活动，日期：{}，小时：{}", activityDate, startHour);
            return null;
        }
        return buildActivityGoodsPage(params, activity);
    }

    private SeckillActivityGoodsPageVO buildActivityGoodsPage(
            SeckillGoodsParamsDTO params,
            SeckillActivity activity
    ) {
        SeckillGoodsParamsDTO queryParams = copyGoodsParams(params);
        queryParams.setActivityId(activity.getId());
        IPage<SeckillGoodsDTO> goodsPage = pageSeckillActivityGoods(queryParams);
        return SeckillActivityGoodsPageVO.builder()
                                         .activity(convertToSeckillActivityVO(activity))
                                         .goodsPage(goodsPage)
                                         .build();
    }

    private SeckillGoodsParamsDTO copyGoodsParams(SeckillGoodsParamsDTO params) {
        SeckillGoodsParamsDTO queryParams = new SeckillGoodsParamsDTO();
        queryParams.setPage(params.getPage());
        queryParams.setPageSize(params.getPageSize());
        queryParams.setMerchantId(params.getMerchantId());
        return queryParams;
    }

    @Override
    public SeckillGoodsWebDetailVO getSeckillGoodsWebDetail(Long seckillGoodsId) {
        SeckillGoods seckillGoods = seckillGoodsService.getById(seckillGoodsId);
        AssertUtils.notNull(seckillGoods, BizErrorCode.SECKILL_GOODS_NOT_FOUND);

        GoodsSku sku = goodsSkuService.getById(seckillGoods.getSkuId());
        AssertUtils.notNull(sku, BizErrorCode.PRODUCT_NOT_FOUND);
        AssertUtils.notNull(sku.getGoodsId(), BizErrorCode.PRODUCT_ID_REQUIRED);

        WebGoodsDetailVO goodsDetail = goodsAppService.getWebGoodsDetail(sku.getGoodsId());
        Integer stock = seckillGoods.getStock() == null ? 0 : seckillGoods.getStock();
        Integer soldCount = seckillGoods.getSoldCount() == null ? 0 : seckillGoods.getSoldCount();

        return SeckillGoodsWebDetailVO.builder()
                                      .seckillGoodsId(seckillGoods.getId())
                                      .activityId(seckillGoods.getActivityId())
                                      .goodsId(sku.getGoodsId())
                                      .selectedSkuId(seckillGoods.getSkuId())
                                      .goodsName(seckillGoods.getGoodsName())
                                      .mainImageUrl(seckillGoods.getMainImageUrl())
                                      .seckillPrice(seckillGoods.getSeckillPrice())
                                      .stock(stock)
                                      .soldCount(soldCount)
                                      .remainingStock(Math.max(stock - soldCount, 0))
                                      .goodsDetail(goodsDetail)
                                      .build();
    }

    private SeckillActivityVO convertToSeckillActivityVO(SeckillActivity seckillActivity) {
        return BeanUtil.copyProperties(seckillActivity, SeckillActivityVO.class);
    }

    // ==================== 私有方法 ====================

    private SeckillGoodsDTO convertApprovedSeckillGoodsToDTO(SeckillGoods seckillGoods) {
        return SeckillGoodsDTO.builder()
                              .id(seckillGoods.getId())
                              .status(AuditStatus.APPROVED.getCode())
                              .skuId(seckillGoods.getSkuId())
                              .goodsName(seckillGoods.getGoodsName())
                              .mainImageUrl(seckillGoods.getMainImageUrl())
                              .seckillPrice(seckillGoods.getSeckillPrice())
                              .stock(seckillGoods.getStock())
                              .build();
    }

    private SeckillGoodsDTO convertAuditItemToDTO(AuditItem item) {
        SeckillGoodsAuditItemDTO snapshot = JsonSupport.fromJson(item.getSnapshot(), SeckillGoodsAuditItemDTO.class);

        return SeckillGoodsDTO.builder()
                              .id(item.getId())
                              .auditItemId(item.getId())
                              .status(item.getStatus())
                              .skuId(snapshot.getSkuId())
                              .goodsName(snapshot.getGoodsName())
                              .mainImageUrl(snapshot.getMainImageUrl())
                              .seckillPrice(snapshot.getSeckillPrice())
                              .stock(snapshot.getStock())
                              .build();
    }

    /**
     * 限流检查
     * 同一用户在60秒内最多发起10次请求
     */
    private void checkRateLimit(Long userId) {
        String rateLimitKey = SECKILL_RATE_LIMIT_KEY_PREFIX + userId;
        Long count = redisTemplate.opsForValue()
                                  .increment(rateLimitKey);

        if (count == 1) {
            redisTemplate.expire(rateLimitKey, RATE_LIMIT_PERIOD, TimeUnit.SECONDS);
        }

        AssertUtils.isTrue(count <= RATE_LIMIT_COUNT, BizErrorCode.SECKILL_RATE_LIMIT_EXCEEDED);
    }

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
     * 扣减库存并创建订单
     * 使用Redis原子操作扣减库存，然后创建秒杀订单
     */
    private Long deductStockAndCreateOrder(Long seckillGoodsId, Long userId, Integer quantity) {
        // 1. 获取秒杀商品信息
        SeckillGoods goods = seckillGoodsService.getById(seckillGoodsId);
        AssertUtils.notNull(goods, BizErrorCode.SECKILL_ACTIVITY_NOT_EXIST);

        // 2. 获取或初始化Redis中的库存
        String stockKey = SECKILL_STOCK_KEY_PREFIX + seckillGoodsId;
        Object stockObj = redisTemplate.opsForValue()
                                       .get(stockKey);
        Long remainingStock;

        if (stockObj == null) {
            // 初始化库存到Redis（如果未初始化）
            remainingStock = goods.getStock()
                                  .longValue();
            redisTemplate.opsForValue()
                         .set(stockKey, remainingStock);
            log.info("秒杀商品库存已初始化到Redis，商品ID：{}，库存：{}", seckillGoodsId, remainingStock);
        } else {
            remainingStock = Long.parseLong(stockObj.toString());
        }

        // 3. 检查库存是否充足
        AssertUtils.isTrue(remainingStock >= quantity, BizErrorCode.SECKILL_STOCK_INSUFFICIENT);

        // 4. 原子性扣减库存
        Long deductedStock = redisTemplate.opsForValue()
                                          .decrement(stockKey, quantity);
        AssertUtils.isTrue(deductedStock >= 0, BizErrorCode.SECKILL_STOCK_INSUFFICIENT);

        // 5. 创建秒杀订单
        SeckillOrder order = SeckillOrder.builder()
                                         .seckillGoodsId(seckillGoodsId)
                                         .activityId(goods.getActivityId())
                                         .productId(goods.getSkuId())
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
     * 检查活动时间有效性（可选验证）
     * 确保活动的日期+小时正确对应当前时间范围
     */
    private void checkActivityTimeValid(SeckillActivity activity) {
        LocalDate activityDate = LocalDate.parse(activity.getActivityDate());
        LocalDateTime activityStart = activityDate.atTime(LocalTime.of(activity.getStartHour(), 0));
        LocalDateTime activityEnd = activityDate.atTime(LocalTime.of(activity.getStartHour() + 1, 0));

        LocalDateTime now = LocalDateTime.now();

        // 活动日期不能是过去的日期
        AssertUtils.isFalse(activityDate.isBefore(LocalDate.now()), BizErrorCode.SECKILL_ACTIVITY_NOT_EXIST);

        log.info("活动时间有效，活动ID：{}，开始时间：{}，结束时间：{}",
                 activity.getId(), activityStart, activityEnd);
    }

    /**
     * 批量初始化所有秒杀商品的库存
     * 将所有已审核通过的秒杀商品库存加载到Redis
     */
    private void initializeAllGoodsStock(Long activityId) {
        log.info("批量初始化活动的秒杀商品库存，活动ID：{}", activityId);

        // 1. 查询该活动的所有秒杀商品
        List<SeckillGoods> allGoods = seckillGoodsService.listByActivityId(activityId);

        if (CollUtil.isEmpty(allGoods)) {
            log.warn("活动中没有秒杀商品，活动ID：{}", activityId);
            return;
        }

        // 2. 逐个初始化库存到Redis
        for (SeckillGoods goods : allGoods) {
            String stockKey = SECKILL_STOCK_KEY_PREFIX + goods.getId();
            redisTemplate.opsForValue()
                         .set(stockKey, goods.getStock()
                                             .longValue());
            log.info("秒杀商品库存已初始化到Redis，商品ID：{}，库存：{}", goods.getId(), goods.getStock());
        }

        log.info("活动所有秒杀商品库存初始化完成，活动ID：{}，商品数：{}", activityId, allGoods.size());
    }
}
