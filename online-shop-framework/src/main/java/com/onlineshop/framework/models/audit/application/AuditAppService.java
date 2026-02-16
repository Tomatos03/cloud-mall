package com.onlineshop.framework.models.audit.application;

import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.models.audit.dto.AuditDecisionDTO;
import com.onlineshop.framework.models.audit.dto.AuditStatusDTO;
import com.onlineshop.framework.models.audit.entity.Audit;
import com.onlineshop.framework.models.audit.enums.AuditStatus;
import com.onlineshop.framework.models.audit.enums.AuditType;
import com.onlineshop.framework.models.audit.service.IAuditService;
import com.onlineshop.framework.utils.AssertUtils;
import com.onlineshop.framework.utils.AuthUserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 审核应用服务实现
 * 负责审核决策的业务流程编排，提供统一的审核操作入口
 *
 * @author Tomatos
 * @date 2026/1/12
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class AuditAppService implements IAuditAppService {
    private final IAuditService auditService;
    private final AuditDelegateFactory auditFactory;

    @Override
    public AuditStatusDTO queryUserCreateStoreAuditStatus() {
        Audit audit = auditService.lambdaQuery()
                                  .eq(Audit::getTargetType, AuditType.STORE_REGISTER.getCode())
                                  .eq(Audit::getApplicantId, AuthUserUtils.getUserId())
                                  .one();
        if (Objects.isNull(audit)) {
            return new AuditStatusDTO();
        }

        return AuditStatusDTO.builder()
                             .status(audit.getStatus())
                             .build();
    }

    @Override
    public void submitAudit(AuditType type, Object payload) {
        IAuditDelegate auditDelegate = auditFactory.getDelegate(type);
        auditDelegate.submitAudit(payload);
    }

    @Override
    public void handleAuditDecision(AuditDecisionDTO decisionDTO) {
        Audit audit = auditService.getById(decisionDTO.getAuditId());
        AuditStatus status = AuditStatus.of(audit.getStatus());
        AssertUtils.notNull(audit, BizErrorCode.AUDIT_LOG_NOT_EXISTS);
        AssertUtils.anyTrue(
                BizErrorCode.AUDIT_INVALID_STATUS,
                status == AuditStatus.PENDING,
                status == AuditStatus.REAUDIT
        );

        AuditType type = AuditType.of(audit.getTargetType());
        IAuditDelegate delegate = auditFactory.getDelegate(type);

        if (decisionDTO.getApproved()) {
            delegate.onAuditApproved(audit);
        } else {
            delegate.onAuditRejected(audit, decisionDTO.getReason());
        }
    }
}