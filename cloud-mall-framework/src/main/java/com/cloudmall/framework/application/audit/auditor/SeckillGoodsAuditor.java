package com.cloudmall.framework.application.audit.auditor;

import cn.hutool.core.collection.CollUtil;
import com.cloudmall.framework.common.enums.BizErrorCode;
import com.cloudmall.framework.models.audit.dto.AuditSubmitDTO;
import com.cloudmall.framework.models.audit.dto.SeckillGoodsAuditItemDTO;
import com.cloudmall.framework.models.audit.entity.Audit;
import com.cloudmall.framework.models.audit.entity.AuditItem;
import com.cloudmall.framework.models.audit.enums.AuditBizType;
import com.cloudmall.framework.models.audit.enums.AuditItemStatus;
import com.cloudmall.framework.models.goods.sku.GoodsSku;
import com.cloudmall.framework.models.goods.sku.IGoodsSkuService;
import com.cloudmall.framework.models.seckill.entity.SeckillActivity;
import com.cloudmall.framework.models.seckill.entity.SeckillGoods;
import com.cloudmall.framework.models.seckill.service.SeckillActivityService;
import com.cloudmall.framework.models.seckill.service.SeckillGoodsService;
import com.cloudmall.framework.utils.AssertUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 秒杀商品申请审核处理器
 * 继承泛型模板基类，实现商家申请商品加入秒杀活动的审核流程
 * <p>
 * 审核流程：
 * 1. 商家提交申请：验证商品、活动、价格等信息 → 创建待审核记录
 * 2. 管理员审核通过：创建 SeckillGoods 记录 → 商品正式加入秒杀活动
 * 3. 管理员审核拒绝：保存拒绝原因
 *
 * @author Tomatos
 * @date 2026/2/26
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillGoodsAuditor extends AbstractAuditor<SeckillGoodsAuditItemDTO> {
    private final SeckillActivityService seckillActivityService;
    private final IGoodsSkuService goodsSkuService;
    private final SeckillGoodsService seckillGoodsService;

    @Override
    protected void validateAndFill(AuditSubmitDTO<SeckillGoodsAuditItemDTO> submitDTO) {
        validateActivity(submitDTO.getBizPid());
        validateAndFillItems(submitDTO.getItems());
    }

    /**
     * 批量处理秒杀商品审核决策
     * <p>
     * 逻辑：
     * 1. 遍历所有审核项
     * 2. 对于通过的项：创建秒杀商品记录
     * 3. 对于拒绝的项：仅记录拒绝原因
     *
     * @param audit 审核批次
     * @param items 批次中的所有项（已按审核决策更新状态）
     */
    @Override
    protected void onProcessed(Audit audit, List<AuditItem> items) {
        AssertUtils.notNull(audit.getBizPid(), BizErrorCode.ACTIVITY_ID_REQUIRED);
        Long auditId = audit.getId();
        log.info("处理秒杀商品审核结果，批次ID: {}，项数: {}", auditId, items.size());

        List<AuditItem> approvedItems = getApprovedItems(items);
        if (CollUtil.isEmpty(approvedItems)) {
            log.info("批次 {} 无通过项，跳过秒杀商品落库", auditId);
            return;
        }

        List<SeckillGoods> seckillGoodsList = buildSeckillGoodsList(approvedItems, audit);
        seckillGoodsService.saveBatch(seckillGoodsList);
        log.info("秒杀商品审核通过批量落库完成，批次ID: {}，落库数量: {}", auditId, seckillGoodsList.size());

        log.info("秒杀商品审核结果处理完成，批次ID: {}", auditId);
    }

    private List<AuditItem> getApprovedItems(List<AuditItem> items) {
        return items.stream()
                    .filter(item -> AuditItemStatus.APPROVED.getCode()
                                                            .equals(item.getStatus()))
                    .collect(Collectors.toList());
    }

    private List<SeckillGoods> buildSeckillGoodsList(List<AuditItem> approvedItems, Audit audit) {
        return approvedItems.stream()
                            .map(item -> convertToSeckillGoods(item, audit))
                            .collect(Collectors.toList());
    }

    private SeckillGoods convertToSeckillGoods(AuditItem item, Audit audit) {
        SeckillGoodsAuditItemDTO seckillItem = parseSnapshot(item.getSnapshot(), SeckillGoodsAuditItemDTO.class);
        return SeckillGoods.builder()
                           .activityId(audit.getBizPid())
                           .skuId(seckillItem.getSkuId())
                           .goodsName(seckillItem.getGoodsName())
                           .mainImageUrl(seckillItem.getMainImageUrl())
                           .merchantId(audit.getApplicantId())
                           .seckillPrice(seckillItem.getSeckillPrice())
                           .stock(seckillItem.getStock())
                           .soldCount(0)
                           .build();
    }

    @Override
    protected boolean support(AuditBizType auditBizType) {
        return AuditBizType.SECKILL_GOODS == auditBizType;
    }

    private void validateActivity(Long activityId) {
        AssertUtils.notNull(activityId, BizErrorCode.ACTIVITY_ID_REQUIRED);
        AssertUtils.notNull(seckillActivityService.getById(activityId), BizErrorCode.SECKILL_ACTIVITY_NOT_EXIST);
    }

    private void validateAndFillItems(Collection<SeckillGoodsAuditItemDTO> items) {
        List<Long> skuIds = new ArrayList<>(items.size());
        for (SeckillGoodsAuditItemDTO item : items) {
            GoodsSku sku = goodsSkuService.getById(item.getSkuId());
            AssertUtils.notNull(sku, BizErrorCode.PRODUCT_NOT_FOUND);

            item.setGoodsName(sku.getGoodsName());
            item.setMainImageUrl(sku.getMainImageUrl());
            item.setStoreId(sku.getStoreId());
            item.setSpecSnapshot(sku.getSpecSnapshot());
            item.setOriginPrice(sku.getPrice());
            skuIds.add(item.getSkuId());
        }

        AssertUtils.isFalse(isAlreadyInRegisterActiveSeckill(skuIds), BizErrorCode.GOODS_ALREADY_IN_SECKILL_ACTIVITY);
    }

    private boolean isAlreadyInRegisterActiveSeckill(Collection<Long> skuIds) {
        if (CollUtil.isEmpty(skuIds)) {
            return false;
        }

        LocalDateTime nextHour = LocalDateTime.now()
                                              .withMinute(0)
                                              .withSecond(0)
                                              .withNano(0)
                                              .plusHours(1);
        String minActivityDate = nextHour.toLocalDate()
                                         .toString();
        int minStartHour = nextHour.getHour();

        List<Long> candidateActivityIds = seckillActivityService.lambdaQuery()
                                                                .select(SeckillActivity::getId)
                                                                .and(wrapper -> wrapper
                                                                        .gt(SeckillActivity::getActivityDate,
                                                                            minActivityDate)
                                                                        .or()
                                                                        .eq(SeckillActivity::getActivityDate,
                                                                            minActivityDate)
                                                                        .ge(SeckillActivity::getStartHour,
                                                                            minStartHour))
                                                                .list()
                                                                .stream()
                                                                .map(SeckillActivity::getId)
                                                                .collect(Collectors.toList());
        if (CollUtil.isEmpty(candidateActivityIds)) {
            return false;
        }

        List<SeckillGoods> existedSeckillGoods = seckillGoodsService.lambdaQuery()
                                                                    .in(SeckillGoods::getSkuId, skuIds)
                                                                    .in(SeckillGoods::getActivityId,
                                                                        candidateActivityIds)
                                                                    .list();
        return CollUtil.isNotEmpty(existedSeckillGoods);
    }
}
