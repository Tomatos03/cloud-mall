package com.onlineshop.framework.models.audit.application.impl;

import com.alibaba.fastjson2.JSON;
import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.models.audit.application.AbstractAuditor;
import com.onlineshop.framework.models.audit.domain.SeckillActivityAuditRequest;
import com.onlineshop.framework.models.audit.enums.AuditStatus;
import com.onlineshop.framework.models.audit.enums.AuditType;
import com.onlineshop.framework.models.audit.service.IAuditService;
import com.onlineshop.framework.models.goods.spu.Goods;
import com.onlineshop.framework.models.goods.spu.IGoodsService;
import com.onlineshop.framework.models.seckill.entity.SeckillActivity;
import com.onlineshop.framework.models.seckill.entity.SeckillGoods;
import com.onlineshop.framework.models.seckill.service.SeckillActivityService;
import com.onlineshop.framework.models.seckill.service.SeckillGoodsService;
import com.onlineshop.framework.utils.AssertUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 秒杀活动审核处理器
 * 继承泛型模板基类，实现秒杀活动审核的完整流程
 * <p>
 * 审核流程（新设计）：
 * 1. 提交审核：验证 → 创建秒杀活动（待审核状态）→ 保存审核记录
 * 2. 审核通过：激活秒杀活动（修改状态为已通过）
 * 3. 审核拒绝：拒绝秒杀活动（修改状态为已拒绝）
 * <p>
 * 职责（已解耦）：
 * 1. 验证秒杀活动审核请求的合法性
 * 2. 创建待审核的秒杀活动对象
 * 3. 生成审核快照用于持久化
 * 4. 处理审核通过时的业务逻辑（激活秒杀活动）
 * 5. 处理审核拒绝时的业务逻辑（拒绝秒杀活动）
 *
 * @author Tomatos
 * @date 2026/2/26
 */
@Component
@RequiredArgsConstructor
public class SeckillActivityAuditor extends AbstractAuditor<SeckillActivityAuditRequest> {
    private final IAuditService auditService;
    private final SeckillActivityService seckillActivityService;
    private final SeckillGoodsService seckillGoodsService;
    private final IGoodsService goodsService;

    @Override
    protected boolean support(AuditType type) {
        return AuditType.SECKILL_ACTIVITY == type;
    }

    @Override
    protected void validateRequest(SeckillActivityAuditRequest request) {
        // 字段非空验证
        AssertUtils.notNull(request.getProductId(), BizErrorCode.PRODUCT_ID_REQUIRED);
        AssertUtils.notNull(request.getStartTime(), BizErrorCode.START_TIME_REQUIRED);
        AssertUtils.notNull(request.getEndTime(), BizErrorCode.END_TIME_REQUIRED);
        AssertUtils.notNull(request.getSeckillPrice(), BizErrorCode.PRICE_REQUIRED);
        AssertUtils.notNull(request.getStock(), BizErrorCode.STOCK_REQUIRED);

        // 时间范围验证
        AssertUtils.isTrue(request.getStartTime()
                                  .isBefore(request.getEndTime()),
                           BizErrorCode.INVALID_TIME_RANGE);

        // 开始时间必须在24小时以后
        LocalDateTime now = LocalDateTime.now();
        AssertUtils.isTrue(request.getStartTime()
                                  .isAfter(now.plusHours(24)),
                           BizErrorCode.ACTIVITY_MUST_ADVANCE_24_HOURS);

        // 时长必须是1小时
        Duration duration = Duration.between(request.getStartTime(), request.getEndTime());
        AssertUtils.isTrue(duration.equals(Duration.ofHours(1)),
                           BizErrorCode.ACTIVITY_DURATION_MUST_BE_ONE_HOUR);

        // 商品存在性验证
        Goods product = goodsService.getById(request.getProductId());
        AssertUtils.notNull(product, BizErrorCode.PRODUCT_NOT_FOUND);

        // 商品状态验证（status = true 表示上架）
        AssertUtils.isTrue(product.getStatus() != null && product.getStatus(),
                           BizErrorCode.PRODUCT_NOT_ACTIVE);

        // 秒杀价格必须低于商品的最高价
        AssertUtils.isTrue(request.getSeckillPrice()
                                  .compareTo(java.math.BigDecimal.valueOf(product.getMaxPrice())) < 0,
                           BizErrorCode.SECKILL_PRICE_MUST_LESS_THAN_ORIGINAL);

        // 商品唯一性检验：不能重复参加待审核或已通过的秒杀
        AuditStatus status = auditService.queryAuditStatus(AuditType.SECKILL_ACTIVITY,
                                                           request.getProductId());
        AssertUtils.isTrue((status != AuditStatus.PENDING && status != AuditStatus.APPROVED),
                           BizErrorCode.PRODUCT_ALREADY_IN_SECKILL);
    }

    @Override
    protected Long onApproved(SeckillActivityAuditRequest request) {
        SeckillActivity activity = new SeckillActivity();
        activity.setProductId(request.getProductId());
        activity.setStartTime(request.getStartTime());
        activity.setEndTime(request.getEndTime());
        activity.setSeckillPrice(request.getSeckillPrice());
        activity.setStock(request.getStock());
        activity.setCreateTime(LocalDateTime.now());
        activity.setUpdateTime(LocalDateTime.now());

        seckillActivityService.save(activity);

        if (request.getActivityId() != null) {
            SeckillGoods seckillGoods = new SeckillGoods();
            seckillGoods.setActivityId(request.getActivityId());
            seckillGoods.setProductId(activity.getProductId());
            seckillGoods.setMerchantId(request.getApplicantId());
            seckillGoods.setSeckillPrice(activity.getSeckillPrice());
            seckillGoods.setStock(activity.getStock());
            seckillGoods.setSoldCount(0);
            seckillGoods.setCreateTime(LocalDateTime.now());
            seckillGoods.setUpdateTime(LocalDateTime.now());

            seckillGoodsService.save(seckillGoods);
        }

        return activity.getId();
    }

    @Override
    protected String generateSnapshot(SeckillActivityAuditRequest request) {
        return JSON.toJSONString(request);
    }

    @Override
    protected SeckillActivityAuditRequest rebuildRequest(String snapshot) {
        return JSON.parseObject(snapshot, SeckillActivityAuditRequest.class);
    }
}