package com.onlineshop.framework.models.audit.application;

import com.onlineshop.framework.exception.BizException;
import com.onlineshop.framework.models.audit.domain.AuditRequest;
import com.onlineshop.framework.models.audit.dto.AuditDecisionDTO;
import com.onlineshop.framework.models.audit.dto.AuditStatusDTO;
import com.onlineshop.framework.models.audit.entity.Audit;
import com.onlineshop.framework.models.audit.enums.AuditType;
import com.onlineshop.framework.models.audit.service.IAuditService;
import com.onlineshop.framework.utils.AuthUserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 审核应用服务实现
 * 提供审核操作的统一API入口
 * 
 * 核心职责：
 * 1. submitAudit - 统一的审核提交API，支持所有业务类型
 * 2. handleAuditDecision - 统一的审核决策API，支持所有业务类型
 * 3. queryUserCreateStoreAuditStatus - 店铺注册状态查询
 * 
 * 设计要点：
 * - 使用工厂模式（AuditorFactory）根据businessType动态获取对应的Auditor
 * - 利用模板方法模式（AbstractAuditor）保证审核流程的一致性
 * - 所有审核类型共享同一套API，降低复杂度
 *
 * @author Tomatos
 * @date 2026/1/12
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class AuditAppService implements IAuditAppService {
    private final IAuditService auditService;
    private final AuditorFactory auditorFactory;

    @Override
    public <T extends AuditRequest> void submitAudit(T request) {
        AbstractAuditor<T> auditor = (AbstractAuditor<T>) auditorFactory.getAuditor(request.getType());
        auditor.submitAudit(request);
    }

    /**
     * 统一的审核决策处理API
     * 处理审核的批准或拒绝，支持所有业务类型
     * 
     * 流程：
     * 1. 根据auditId查询审核记录
     * 2. 从审核记录中获取targetType（即businessType）
     * 3. 通过AuditorFactory获取对应的Auditor实例
     * 4. 调用Auditor的handleDecision()方法，进行批准或拒绝处理
     * 
     * @param decision 审核决策DTO，包含auditId、approved标志和拒绝原因
     * @throws BizException 当审核记录不存在或业务类型未知时抛出
     */
    @Override
    public void handleAuditDecision(AuditDecisionDTO decision, String type) {
        AbstractAuditor<?> auditor = auditorFactory.getAuditor(type);
        auditor.handleDecision(decision);
    }

    /**
     * 查询当前用户的创建店铺审核状态
     */
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
}