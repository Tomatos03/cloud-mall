package com.onlineshop.framework.models.audit.application.impl;

import com.alibaba.fastjson2.JSON;
import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.models.audit.application.AbstractAuditor;
import com.onlineshop.framework.models.audit.domain.SeckillGoodsAuditRequest;
import com.onlineshop.framework.models.audit.domain.SeckillGoodsItem;
import com.onlineshop.framework.models.audit.enums.AuditStatus;
import com.onlineshop.framework.models.audit.enums.AuditType;
import com.onlineshop.framework.models.audit.service.IAuditService;
import com.onlineshop.framework.models.goods.spu.Goods;
import com.onlineshop.framework.models.seckill.enums.SeckillActivityStatus;
import com.onlineshop.framework.models.goods.spu.IGoodsService;
import com.onlineshop.framework.models.seckill.entity.SeckillActivity;
import com.onlineshop.framework.models.seckill.entity.SeckillGoods;
import com.onlineshop.framework.models.seckill.service.SeckillActivityService;
import com.onlineshop.framework.models.seckill.service.SeckillGoodsService;
import com.onlineshop.framework.utils.AssertUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 秒杀商品申请审核处理器
 * 继承泛型模板基类，实现商家申请商品加入秒杀活动的审核流程
 * <p>
 * 审核流程：
 * 1. 商家提交申请：验证商品、活动、价格等信息 → 创建待审核记录
 * 2. 管理员审核通过：创建 SeckillGoods 记录 → 商品正式加入秒杀活动
 * 3. 管理员审核拒绝：保存拒绝原因
 * <p>
 * 验证规则：
 * 1. 活动存在且状态为"报名中"
 * 2. 商品存在且已上架
 * 3. 秒杀库存 ≤ 商品库存
 * 4. 秒杀价格 < 商品原价
 * 5. 商品唯一性：不能重复申请参加待审核或已通过的秒杀
 * 6. 活动是否已达到最大商品数（审核通过时检查）
 *
 * @author Tomatos
 * @date 2026/2/26
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillGoodsAuditor extends AbstractAuditor<SeckillGoodsAuditRequest> {
    private final IAuditService auditService;
    private final SeckillActivityService seckillActivityService;
    private final SeckillGoodsService seckillGoodsService;
    private final IGoodsService goodsService;

    @Override
    protected boolean support(AuditType type) {
        return AuditType.SECKILL_ACTIVITY == type;
    }

    @Override
    protected void validateRequest(SeckillGoodsAuditRequest request) {
        log.info("验证秒杀商品申请：活动ID={}, 商品数量={}, 申请商家={}",
                 request.getActivityId(), request.getItems().size(), request.getApplicantId());

        // ==================== 基础验证 ====================
        AssertUtils.notNull(request.getActivityId(), BizErrorCode.ACTIVITY_ID_REQUIRED);
        AssertUtils.notNull(request.getItems(), BizErrorCode.ITEMS_REQUIRED);
        AssertUtils.isTrue(!request.getItems().isEmpty(), BizErrorCode.ITEMS_EMPTY);

        // ==================== 活动验证 ====================
        SeckillActivity activity = seckillActivityService.getById(request.getActivityId());
        AssertUtils.notNull(activity, BizErrorCode.SECKILL_ACTIVITY_NOT_EXIST);

        // 活动必须处于"报名中"状态，商家才能申请加入
        AssertUtils.isTrue(activity.getStatus() != null && activity.getStatus().equals(SeckillActivityStatus.REGISTRATION.getCode()),
                           BizErrorCode.INVALID_ACTIVITY_STATUS);

        // ==================== 逐个商品验证 ====================
        for (int i = 0; i < request.getItems().size(); i++) {
            SeckillGoodsItem item = request.getItems().get(i);
            log.info("验证第 {} 个商品：productId={}, price={}, stock={}",
                     i + 1, item.getProductId(), item.getSeckillPrice(), item.getStock());

            // 字段非空验证
            AssertUtils.notNull(item.getProductId(), BizErrorCode.PRODUCT_ID_REQUIRED);
            AssertUtils.notNull(item.getSeckillPrice(), BizErrorCode.PRICE_REQUIRED);
            AssertUtils.notNull(item.getStock(), BizErrorCode.STOCK_REQUIRED);

            // 商品存在性和状态验证
            Goods product = goodsService.getById(item.getProductId());
            AssertUtils.notNull(product, BizErrorCode.PRODUCT_NOT_FOUND);
            AssertUtils.isTrue(product.getStatus() != null && product.getStatus(),
                              BizErrorCode.PRODUCT_NOT_ACTIVE);

            // 价格验证
            AssertUtils.isTrue(
                item.getSeckillPrice().compareTo(java.math.BigDecimal.valueOf(product.getMaxPrice())) < 0,
                BizErrorCode.SECKILL_PRICE_MUST_LESS_THAN_ORIGINAL
            );

            // 库存验证
            AssertUtils.isTrue(item.getStock() > 0, BizErrorCode.SECKILL_STOCK_EXCEEDS_PRODUCT_STOCK);

            // 商品唯一性验证（不能重复申请）
            AuditStatus status = auditService.queryAuditStatus(AuditType.SECKILL_ACTIVITY, item.getProductId());
            AssertUtils.isTrue((status != AuditStatus.PENDING && status != AuditStatus.APPROVED),
                              BizErrorCode.PRODUCT_ALREADY_IN_SECKILL);
        }

        // ==================== 活动容量检查 ====================
        if (activity.getMaxItems() != null && activity.getMaxItems() > 0) {
            long approvedGoodsCount = seckillGoodsService.countByActivityId(request.getActivityId());
            long applicableCount = approvedGoodsCount + request.getItems().size();
            AssertUtils.isTrue(applicableCount <= activity.getMaxItems(),
                              BizErrorCode.ACTIVITY_MAX_ITEMS_REACHED);
        }

        log.info("秒杀商品申请验证通过：活动ID={}, 商品数量={}",
                 request.getActivityId(), request.getItems().size());
    }

    @Override
    protected String generateSnapshot(SeckillGoodsAuditRequest request) {
        return JSON.toJSONString(request);
    }

    @Override
    protected Long onApproved(SeckillGoodsAuditRequest request) {
        log.info("秒杀商品申请通过，开始创建秒杀商品记录：活动ID={}, 商品数量={}",
                 request.getActivityId(), request.getItems().size());

        // ==================== 批量创建秒杀商品记录 ====================
        // 注：活动容量检查已在 validateRequest() 中完成，此处无需重复检查
        java.util.List<SeckillGoods> goodsList = new java.util.ArrayList<>(request.getItems().size());
        
        for (SeckillGoodsItem item : request.getItems()) {
            SeckillGoods seckillGoods = new SeckillGoods();
            seckillGoods.setActivityId(request.getActivityId());
            seckillGoods.setProductId(item.getProductId());
            seckillGoods.setMerchantId(request.getApplicantId());
            seckillGoods.setSeckillPrice(item.getSeckillPrice());
            seckillGoods.setStock(item.getStock());
            seckillGoods.setSoldCount(0);
            
            goodsList.add(seckillGoods);
        }
        
        // 使用 saveBatch() 一次性保存所有记录（比循环 save() 性能更好）
        seckillGoodsService.saveBatch(goodsList);
        
        Long firstGoodsId = goodsList.isEmpty() ? null : goodsList.get(0).getId();
        log.info("批量创建完成：共 {} 个商品，首条记录ID={}",
                 request.getItems().size(), firstGoodsId);
        return firstGoodsId;
    }

    @Override
    protected SeckillGoodsAuditRequest rebuildRequest(String snapshot) {
        return JSON.parseObject(snapshot, SeckillGoodsAuditRequest.class);
    }
}
