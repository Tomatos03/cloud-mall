package com.onlineshop.framework.models.audit.application;

import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.models.audit.domain.AuditRequest;
import com.onlineshop.framework.models.audit.dto.AuditDecisionDTO;
import com.onlineshop.framework.models.audit.entity.Audit;
import com.onlineshop.framework.models.audit.enums.AuditStatus;
import com.onlineshop.framework.models.audit.enums.AuditType;
import com.onlineshop.framework.models.audit.service.IAuditService;
import com.onlineshop.framework.utils.AssertUtils;
import com.onlineshop.framework.utils.AuthUserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;

/**
 * 泛型审核模板基类
 * 定义审核流程的骨架，让子类实现业务特定的审核逻辑
 * <p>
 * 使用模板方法模式 + 泛型，实现：
 * 1. 统一的审核提交流程（验证 → 规则检查 → 快照 → 保存）
 * 2. 分离的审核决策流程（批准或拒绝）
 * 3. 类型安全的业务特定参数（T extends AuditRequest）
 * 4. 易于扩展的新业务支持
 *
 * @author Tomatos
 * @date 2026/2/26
 */
public abstract class AbstractAuditor<T extends AuditRequest> {
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    protected IAuditService auditService;

    public final void submitAudit(T request) {
        validateRequest(request);
        transactionTemplate.execute(status -> {
            saveAuditRecord(request, generateSnapshot(request));
            return null;
        });
    }

    protected abstract boolean support(AuditType type);

    protected abstract void validateRequest(T request);

    /**
     * 保存审核记录到数据库
     * <p>
     * 此方法在提交审核时被调用，保存一条新的待审核记录。
     * 不需要决策结果，因为此时审核记录的状态为 PENDING，等待管理员审批。
     * 此时 targetId 为 null，审核通过时才会设置。
     *
     * @param request  审核请求
     * @param snapshot 快照信息
     */
    protected void saveAuditRecord(T request, String snapshot) {
        Audit audit = new Audit();
        audit.setTargetType(request.getType());
        audit.setTargetId(request.getTargetId());
        audit.setStatus(AuditStatus.PENDING.getCode());
        audit.setApplicantId(AuthUserUtils.getUserId());
        audit.setApplicantName(AuthUserUtils.getUsername());
        audit.setSnapshot(snapshot);
        audit.setCreateTime(LocalDateTime.now());

        auditService.save(audit);
    }

    /**
     * 生成快照
     * 子类在此方法中生成需要保存的快照信息
     * 快照用于审核记录的持久化和后续的恢复
     *
     * @param request 审核请求
     * @return 快照JSON字符串
     */
    protected abstract String generateSnapshot(T request);

    /**
     * 处理审核决策（通过或拒绝）
     * <p>
     * 统一的决策处理入口，根据决策结果路由到对应的处理方法
     *
     * @param decision 审核决策DTO（包含审核ID、决策结果、拒绝原因等）
     */
    public final void handleDecision(AuditDecisionDTO decision) {
        Audit audit = auditService.getById(decision.getAuditId());
        AssertUtils.notNull(audit, BizErrorCode.AUDIT_LOG_NOT_EXISTS);

        fillAuditorInfo(audit);
        transactionTemplate.execute(status -> {
            if (decision.getApproved()) {
                Long targetId = onApproved(rebuildRequest(audit.getSnapshot()));
                audit.setTargetId(targetId);
                audit.setStatus(AuditStatus.APPROVED.getCode());
                auditService.updateById(audit);
            } else {
                audit.setStatus(AuditStatus.REJECTED.getCode());
                audit.setReason(decision.getReason());
                auditService.updateById(audit);
            }
            return null;
        });
    }

    private void fillAuditorInfo(Audit audit) {
        audit.setAuditTime(LocalDateTime.now());
        audit.setAuditorId(AuthUserUtils.getUserId());
        audit.setAuditorName(AuthUserUtils.getUsername());
    }

    /**
     * 审核通过的抽象方法
     * 子类实现此方法创建业务对象并返回其ID
     *
     * @param request 从快照重建的审核请求
     * @return 创建的业务对象的ID
     */
    protected abstract Long onApproved(T request);

    /**
     * 从快照重建审核请求对象
     * 审核通过时，需要从快照JSON反序列化为原始的审核请求对象
     *
     * @param snapshot 快照JSON字符串
     * @return 重建的审核请求对象
     */
    protected abstract T rebuildRequest(String snapshot);
}