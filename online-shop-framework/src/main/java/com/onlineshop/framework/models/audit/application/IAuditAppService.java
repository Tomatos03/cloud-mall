package com.onlineshop.framework.models.audit.application;

import com.onlineshop.framework.models.audit.dto.AuditDecisionDTO;
import com.onlineshop.framework.models.audit.dto.AuditStatusDTO;
import com.onlineshop.framework.models.audit.enums.AuditStatus;
import com.onlineshop.framework.models.audit.enums.AuditType;

/**
 *
 * @author Tomatos
 * @date 2026/1/12
 */
public interface IAuditAppService {
    AuditStatusDTO queryUserCreateStoreAuditStatus();

    /**
     * 提交审核
     * 将业务对象信息序列化后提交到审核系统
     *
     * @param payload 业务对象的请求体（泛型类型T）
     */
    void submitAudit(AuditType type, Object payload);

    /**
     * 处理审核决策（同意或拒绝）
     * 包括：
     * 1. 更新审核记录的状态
     * 2. 如果审核通过，更新目标对象（如商品）并更新审核记录的targetId
     * 3. 如果审核拒绝，记录拒绝原因
     *
     * @param decisionDTO 审核决定数据（包含auditId, approved, reason）
     */
    void handleAuditDecision(AuditDecisionDTO decisionDTO);
}