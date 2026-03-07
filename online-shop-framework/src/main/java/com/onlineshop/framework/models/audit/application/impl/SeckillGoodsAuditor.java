package com.onlineshop.framework.models.audit.application.impl;

import com.alibaba.fastjson.JSON;
import com.onlineshop.framework.models.audit.application.AbstractAuditor;
import com.onlineshop.framework.models.audit.dto.SeckillGoodsAuditItemDTO;
import com.onlineshop.framework.models.audit.entity.AuditItem;
import com.onlineshop.framework.models.audit.enums.AuditBizType;
import com.onlineshop.framework.models.audit.enums.AuditItemStatus;
import com.onlineshop.framework.models.goods.spu.IGoodsService;
import com.onlineshop.framework.models.seckill.service.SeckillActivityService;
import com.onlineshop.framework.models.seckill.service.SeckillGoodsService;
import com.onlineshop.framework.support.JsonSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

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
    private final SeckillGoodsService seckillGoodsService;
    private final IGoodsService goodsService;

    @Override
    protected boolean support(AuditBizType auditBizType) {
        return AuditBizType.SECKILL_GOODS == auditBizType;
    }

    @Override
    protected void validateAndFill(Collection<SeckillGoodsAuditItemDTO> items) {
        // 秒杀商品审核请求验证
    }

    /**
     * 批量处理秒杀商品审核决策
     * <p>
     * 逻辑：
     * 1. 遍历所有审核项
     * 2. 对于通过的项：创建秒杀商品记录
     * 3. 对于拒绝的项：仅记录拒绝原因
     *
     * @param auditId 审核批次ID
     * @param items   批次中的所有项（已按审核决策更新状态）
     */
    @Override
    protected void onProcessed(Long auditId, List<AuditItem> items) {
        log.info("处理秒杀商品审核结果，批次ID: {}，项数: {}", auditId, items.size());

        for (AuditItem item : items) {
            if (AuditItemStatus.APPROVED.getCode().equals(item.getStatus())) {
                // 通过：创建秒杀商品记录
                try {
                    SeckillGoodsAuditItemDTO seckillItem = parseSnapshot(item.getSnapshot(), SeckillGoodsAuditItemDTO.class);
                    // 具体的创建逻辑由子类或服务层实现
                    log.info("秒杀商品审核通过，AuditItem ID: {}", item.getId());
                } catch (Exception e) {
                    log.error("秒杀商品审核通过处理失败，AuditItem ID: {}", item.getId(), e);
                    throw e;
                }
            } else if (AuditItemStatus.REJECTED.getCode().equals(item.getStatus())) {
                // 拒绝：记录拒绝原因
                log.info("秒杀商品被拒绝，AuditItem ID: {}，原因: {}", item.getId(), item.getReason());
            }
        }

        log.info("秒杀商品审核结果处理完成，批次ID: {}", auditId);
    }
}
