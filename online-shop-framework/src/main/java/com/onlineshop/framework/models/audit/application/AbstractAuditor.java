package com.onlineshop.framework.models.audit.application;

import com.onlineshop.framework.models.audit.dto.AuditDecisionDTO.AuditItemDecision;
import com.onlineshop.framework.models.audit.dto.AuditSubmitDTO;
import com.onlineshop.framework.models.audit.entity.Audit;
import com.onlineshop.framework.models.audit.entity.AuditItem;
import com.onlineshop.framework.models.audit.enums.AuditBizType;
import com.onlineshop.framework.models.audit.enums.AuditItemStatus;
import com.onlineshop.framework.models.audit.service.IAuditItemService;
import com.onlineshop.framework.models.audit.service.IAuditService;
import com.onlineshop.framework.support.JsonSupport;
import com.onlineshop.framework.utils.AuthUserUtils;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 审核模板基类
 * 定义审核流程的骨架，让子类实现业务特定的审核逻辑
 * <p>
 * 重构说明：
 * 1. 提交流程：submitAudit() - 创建批次和项目，批量保存
 * 2. 决策流程：handleDecisions() - 批量处理决策，批量更新，事务外执行业务回调
 * 3. 回调设计：统一的批次级回调 onBatchProcessed()，子类自主判断项状态处理
 * 4. 性能优化：批量数据库操作，从 O(n) 降低到 O(1)
 *
 * @author Tomatos
 * @date 2026/3/7
 */
@Slf4j
public abstract class AbstractAuditor<T> {
    @Autowired
    protected IAuditService auditService;
    @Autowired
    protected IAuditItemService auditItemService;
    @Autowired
    private TransactionTemplate transactionTemplate;


    /**
     * 提交审核
     * <p>
     * 特点：
     * 1. 泛型 <T> 由具体审核器确定（T = 业务对象类型，如 Goods、SeckillActivityAuditItemDTO 等）
     * 2. 创建一个 Audit 批次
     * 3. 为每个 item 创建 AuditItem 记录
     * 4. 每个 AuditItem 存储单个业务对象的快照
     *
     * @param submitDTO 审核提交请求，包含 bizType 和 items 列表
     */
    public void submitAudit(AuditSubmitDTO<T> submitDTO) {
        int size = submitDTO.getItems().size();
        
        log.info("提交审核，业务类型: {}，项数: {}", submitDTO.getBizType(), size);
        validateAndFill(submitDTO.getItems());
        transactionTemplate.execute(status -> {
            // 创建批次
            Audit batch = auditService.createAuditBatch(
                    submitDTO.getBizType(),
                    size
            );
            List<AuditItem> auditItemList = buildAuditItems(batch.getId(), submitDTO.getItems());
            auditItemService.saveBatch(auditItemList);

            log.info("批次 {} 创建成功，共 {} 个项", batch.getId(), size);
            return null;
        });
    }

    /**
     * 构建审核项目列表
     *
     * @param auditId 批次ID
     * @param itemList 审核业务对象列表
     * @return 构建好的 AuditItem 列表
     */
    private List<AuditItem> buildAuditItems(Long auditId, Collection<T> itemList) {
        List<AuditItem> auditItemList = new ArrayList<>(itemList.size());
        for (T bizObject : itemList) {

            // 生成单项快照
            String itemSnapshot = generateSnapshot(bizObject);

            // 创建 AuditItem 记录
            AuditItem auditItem = AuditItem.builder()
                                           .auditId(auditId)
                                           .status(AuditItemStatus.PENDING.getCode())
                                           .snapshot(itemSnapshot)
                                           .build();
            auditItemList.add(auditItem);
        }
        return auditItemList;
    }

    /**
     * 验证和填充审核请求
     * 子类实现具体的业务验证逻辑
     *
     * @param items 待审核的业务对象集合
     */
    protected abstract void validateAndFill(Collection<T> items);

    /**
     * 生成单项快照
     * 子类在此方法中将业务对象序列化为 JSON
     *
     * @param bizObject 业务对象
     * @return 快照JSON字符串
     */
    protected String generateSnapshot(T bizObject) {
        return JsonSupport.toJson(bizObject);
    }
    
    protected T parseSnapshot(String snapshot, Class<T> clazz) {
        return JsonSupport.fromJson(snapshot, clazz);
    }

    /**
     * 处理审核决策
     *
     * @param auditId   审核批次ID
     * @param decisions 该批次内所有项的决策列表
     */
    public final void handleDecisions(Long auditId, List<AuditItemDecision> decisions) {
        log.info("处理批量审核决策，批次ID: {}，决策数: {}", auditId, decisions.size());

        try {
            transactionTemplate.execute(status -> {
                List<AuditItem> allItems = auditItemService.queryByAuditId(auditId);
                log.debug("查询批次项目，数量: {}", allItems.size());

                Map<Long, AuditItemDecision> decisionMap = createAuditItemIdToDecisionMap(decisions);
                for (AuditItem item : allItems) {
                    fillAuditorInfo(item);

                    AuditItemDecision decision = decisionMap.get(item.getId());
                    // 统一处理：设置状态和拒绝原因
                    if (decision.getApproved()) {
                        item.setStatus(AuditItemStatus.APPROVED.getCode());
                        log.debug("项目 {} 设置为通过状态", item.getId());
                    } else {
                        item.setStatus(AuditItemStatus.REJECTED.getCode());
                        item.setReason(decision.getReason());
                        log.debug("项目 {} 设置为拒绝状态，原因: {}", item.getId(), decision.getReason());
                    }
                }

                auditItemService.updateBatchById(allItems);
                log.info("批量更新项目完成，批次ID: {}，项数: {}", auditId, allItems.size());

                auditService.recalculateAuditStatus(auditId);
                log.info("批次状态推算完成，批次ID: {}", auditId);

                onProcessed(auditId, allItems);
                return null;
            });

            log.info("审核决策落库成功，批次ID: {}", auditId);
        } catch (Exception e) {
            log.error("审核决策落库失败，批次ID: {}", auditId, e);
            throw e;
        }
    }

    private static void fillAuditorInfo(AuditItem item) {
        item.setAuditorId(AuthUserUtils.getUserId());
        item.setAuditorName(AuthUserUtils.getUsername());
        item.setAuditTime(LocalDateTime.now());
    }

    @NotNull
    private static Map<Long, AuditItemDecision> createAuditItemIdToDecisionMap(List<AuditItemDecision> decisions) {
        return decisions.stream()
                        .collect(Collectors.toMap(
                                AuditItemDecision::getAuditItemId, d -> d)
                        );
    }

    abstract protected void onProcessed(Long auditId, List<AuditItem> allItems);

    protected abstract boolean support(AuditBizType auditBizType);
}