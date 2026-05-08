package com.onlineshop.framework.application.seckill;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.event.MQTag;
import com.onlineshop.framework.event.MQTopicProperties;
import com.onlineshop.framework.event.TransactionCommitSendMQEvent;
import com.onlineshop.framework.exception.BizException;
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
import com.onlineshop.framework.application.seckill.vo.SeckillActivityGoodsPageVO;
import com.onlineshop.framework.application.seckill.vo.SeckillGoodsWebDetailVO;
import com.onlineshop.framework.application.seckill.vo.SeckillParticipateResultVO;
import com.onlineshop.framework.models.seckill.dto.SeckillGoodsDTO;
import com.onlineshop.framework.models.seckill.dto.SeckillGoodsParamsDTO;
import com.onlineshop.framework.models.seckill.entity.SeckillActivity;
import com.onlineshop.framework.models.seckill.entity.SeckillGoods;
import com.onlineshop.framework.models.seckill.entity.SeckillOrder;
import com.onlineshop.framework.models.seckill.enums.SeckillActivityStatus;
import com.onlineshop.framework.models.seckill.enums.SeckillOrderStatus;
import com.onlineshop.framework.models.seckill.enums.SeckillStockLuaResult;
import com.onlineshop.framework.models.seckill.service.SeckillActivityService;
import com.onlineshop.framework.models.seckill.service.SeckillGoodsService;
import com.onlineshop.framework.models.seckill.service.SeckillOrderService;
import com.onlineshop.framework.models.seckill.vo.SeckillActivityVO;
import com.onlineshop.framework.support.JsonSupport;
import com.onlineshop.framework.utils.AssertUtils;
import com.onlineshop.framework.utils.AuthUserUtils;
import com.onlineshop.framework.utils.IDNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    private static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
            "yyyy-MM-dd HH:mm:ss"
    );
    private static final int DEFAULT_ACTIVITY_DURATION_HOURS = 1;
    private static final String SECKILL_GOODS_KEY_PREFIX = "seckill:goods:";
    private static final String SECKILL_STOCK_KEY_PREFIX = "seckill:stock:";
    private static final String SECKILL_USER_ORDER_KEY_PREFIX = "seckill:user:order:";
    private static final int USER_ORDER_MARK_TTL_SECONDS = 30 * 24 * 60 * 60;
    private static final String LUA_PRE_DEDUCT_STOCK_SCRIPT_PATH = "script/seckill_pre_deduct_stock.lua";
    private static final RedisScript<Integer> PRE_DEDUCT_STOCK_SCRIPT;
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
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private MQTopicProperties mqTopicProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SeckillParticipateResultVO participateSeckill(Long seckillGoodsId, Integer quantity) {
        Long userId = AuthUserUtils.getUserId();
        log.info("用户 {} 参与秒杀商品 {}，购买数量：{}", userId, seckillGoodsId, quantity);
        SeckillGoods seckillGoods = querySeckillGoodsFromCache(seckillGoodsId);
        checkSeckillStatus(seckillGoods);
        SeckillParticipateResultVO result = deductStockAndCreateOrder(seckillGoods, userId, quantity);
        applicationEventPublisher.publishEvent(
                new TransactionCommitSendMQEvent(
                        mqTopicProperties.getSeckill(),
                        MQTag.SECKILL_ORDER_CREATE,
                        result.getOrderId()
                )
        );
        return result;
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
                targetTime.toLocalDate()
                          .toString(),
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

    private SeckillActivityVO convertToSeckillActivityVO(SeckillActivity seckillActivity) {
        return BeanUtil.copyProperties(seckillActivity, SeckillActivityVO.class);
    }

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

    // ==================== 私有方法 ====================

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

    private SeckillGoods querySeckillGoodsFromCache(Long seckillGoodsId) {
        String goodsKey = SECKILL_GOODS_KEY_PREFIX + seckillGoodsId;
        Object cacheObject = redisTemplate.opsForValue()
                                          .get(goodsKey);
        AssertUtils.notNull(cacheObject, BizErrorCode.SECKILL_GOODS_NOT_FOUND);
        SeckillGoods seckillGoods;
        if (cacheObject instanceof SeckillGoods) {
            seckillGoods = (SeckillGoods) cacheObject;
            return seckillGoods;
        }
        throw new BizException(BizErrorCode.SECKILL_GOODS_NOT_FOUND);
    }

    /**
     * 检查秒杀活动时间状态
     * 优先使用 Redis 中的活动状态，其次使用 Redis 中的活动时间判断
     *
     * @param seckillGoods 秒杀商品缓存对象
     */
    private void checkSeckillStatus(SeckillGoods seckillGoods) {
        SeckillActivityStatus seckillActivityStatus = SeckillActivityStatus.of(seckillGoods.getActivityStatus());
        AssertUtils.isFalse(SeckillActivityStatus.REGISTRATION == seckillActivityStatus,
                            BizErrorCode.SECKILL_NOT_STARTED);
        AssertUtils.isFalse(SeckillActivityStatus.ENDED == seckillActivityStatus,
                            BizErrorCode.SECKILL_ALREADY_ENDED);
    }

    /**
     * 扣减库存并创建订单
     * 使用Redis原子操作扣减库存，然后创建秒杀订单
     */
    private SeckillParticipateResultVO deductStockAndCreateOrder(
            SeckillGoods seckillGoods,
            Long userId,
            Integer quantity
    ) {
        Long seckillGoodsId = seckillGoods.getId();
        // 2. Lua原子预扣减库存（库存需在活动开始前预热到Redis）
        String stockKey = SECKILL_STOCK_KEY_PREFIX + seckillGoodsId;
        String userOrderKey = buildUserOrderKey(userId, seckillGoodsId);
        Integer deductedStock = redisTemplate.execute(
                PRE_DEDUCT_STOCK_SCRIPT,
                Arrays.asList(stockKey, userOrderKey),
                quantity,
                USER_ORDER_MARK_TTL_SECONDS
        );
        AssertUtils.notNull(deductedStock, BizErrorCode.SECKILL_FAILED);
        if (deductedStock < 0) {
            SeckillStockLuaResult seckillResult = SeckillStockLuaResult.of(deductedStock);
            switch (seckillResult) {
                case STOCK_NOT_INIT -> throw new BizException(BizErrorCode.SECKILL_CACHE_INIT_FAILED);
                case STOCK_NOT_ENOUGH -> throw new BizException(BizErrorCode.SECKILL_STOCK_INSUFFICIENT);
                case REPEAT_ORDER -> throw new BizException(BizErrorCode.SECKILL_REPEAT_ORDER);
                default -> throw new BizException(BizErrorCode.SECKILL_FAILED);
            }
        }

        // 3. 创建秒杀订单（未支付）
        SeckillOrder order = SeckillOrder.builder()
                                         .goodsId(seckillGoodsId)
                                         .userId(userId)
                                         .orderNo(IDNumber.generateOrderNo())
                                         .price(seckillGoods.getSeckillPrice())
                                         .quantity(quantity)
                                         .status(SeckillOrderStatus.UNPAID.getCode())
                                         .build();

        boolean saved = seckillOrderService.save(order);
        AssertUtils.isTrue(saved, BizErrorCode.SECKILL_FAILED);
        log.info("秒杀订单已创建，订单ID：{}，商品ID：{}，用户ID：{}", order.getId(), seckillGoodsId, userId);

        return SeckillParticipateResultVO.builder()
                                         .orderId(order.getId())
                                         .message("秒杀请求已受理，订单正在创建中")
                                         .build();
    }

    private String buildUserOrderKey(Long userId, Long seckillGoodsId) {
        return SECKILL_USER_ORDER_KEY_PREFIX + userId + ":" + seckillGoodsId;
    }

    static {
        DefaultRedisScript<Integer> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource(LUA_PRE_DEDUCT_STOCK_SCRIPT_PATH)));
        redisScript.setResultType(Integer.class);
        PRE_DEDUCT_STOCK_SCRIPT = redisScript;
    }
}
