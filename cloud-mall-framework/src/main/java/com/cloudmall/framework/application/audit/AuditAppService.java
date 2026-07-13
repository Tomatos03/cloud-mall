package com.cloudmall.framework.application.audit;

import com.cloudmall.framework.application.audit.auditor.AbstractAuditor;
import com.cloudmall.framework.application.audit.auditor.AuditorFactory;
import com.cloudmall.framework.exception.BizException;
import com.cloudmall.framework.models.audit.dto.AuditStatusDTO;
import com.cloudmall.framework.models.audit.dto.AuditDecisionDTO;
import com.cloudmall.framework.models.audit.entity.Audit;
import com.cloudmall.framework.models.audit.enums.AuditBizType;
import com.cloudmall.framework.models.audit.enums.AuditStatus;
import com.cloudmall.framework.models.audit.service.IAuditService;
import com.cloudmall.framework.models.audit.service.IAuditItemService;
import com.cloudmall.framework.utils.AssertUtils;
import com.cloudmall.framework.context.AuthUserContext;
import com.cloudmall.framework.common.enums.BizErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 审核应用服务实现
 * 提供审核操作的统一API入口
 * 
 * 核心职责：
 * 1. submitBatchAuditDecisions - 批量审核决策分发（新流程）
 * 2. queryUserCreateStoreAuditStatus - 店铺注册状态查询
 * 3. getSeckillActivityGoods - 秒杀活动商品查询
 * 
 * 设计要点：
 * - 使用工厂模式（AuditorFactory）根据auditId获取Audit的busType，动态获取对应的Auditor
 * - 利用模板方法模式（AbstractAuditor）保证审核流程的一致性
 * - 批量决策原子性：所有item都审批完成后，才推算Audit状态
 *
 * @author Tomatos
 * @date 2026/1/12
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class AuditAppService implements IAuditAppService {
    private final IAuditService auditService;
    private final IAuditItemService auditItemService;
    private final AuditorFactory auditorFactory;

    /**
     * 批量审核决策分发API
     * 处理一个审核批次的所有项目决策，支持所有业务类型
     * <p>
     * 流程：
     * 1. 根据auditId查询审核批次记录
     * 2. 从审核批次中获取busType（业务类型）
     * 3. 通过AuditorFactory获取对应的Auditor实例
     * 4. 调用Auditor的handleBatchDecisions()方法，批量处理所有决策（原子性）
     * 5. Auditor完成后自动推算Audit的状态（PENDING→APPROVED/REJECTED/PARTIAL）
     *
     * @param batchDecision 批量决策DTO，包含auditId和所有项目的decisions列表
     * @param type
     * @throws BizException 当审核批次不存在或业务类型未知时抛出
     */
    @Override
    public void submitAuditDecisions(AuditDecisionDTO batchDecision, String type) {
        AbstractAuditor<?> auditor = auditorFactory.getAuditor(type);
        auditor.handleDecisions(batchDecision.getAuditNo(), batchDecision.getDecisions());
        log.info("批量审核决策处理完成，审核批次ID: {}", batchDecision.getAuditNo());
    }

    /**
     * 查询当前用户的创建店铺审核状态
     */
    @Override
    public AuditStatusDTO queryUserCreateStoreAuditStatus() {
        Audit audit = auditService.lambdaQuery()
                                  .eq(Audit::getBizType, AuditBizType.STORE_REGISTER.getCode())
                                  .eq(Audit::getApplicantId, AuthUserContext.getUserId())
                                  .one();
        if (Objects.isNull(audit)) {
            return new AuditStatusDTO();
        }

        return AuditStatusDTO.builder()
                             .status(audit.getStatus())
                             .build();
    }

    /**
     * 撤销审核申请
     * 将审核批次和其下所有项目的状态改为已撤销
     *
     * @param auditNo 审核批次编号
     */
    @Override
    public void withdrawAudit(String auditNo) {
        // 1. 获取审核批次
        Audit audit = auditService.lambdaQuery()
                                  .eq(Audit::getAuditNo, auditNo)
                                  .one();
        AssertUtils.notNull(audit, BizErrorCode.AUDIT_NOT_EXIST);

        // 2. 验证权限：只有申请人才能撤销
        AssertUtils.isEqual(AuthUserContext.getUserId(), audit.getApplicantId(), BizErrorCode.NO_PERMISSION);

        // 3. 验证状态：只有待审核状态才能撤销
        AssertUtils.isEqual(audit.getStatus(), AuditStatus.PENDING.getCode(), BizErrorCode.AUDIT_INVALID_STATUS);

        // 4. 更新批次状态为已撤销
        Audit updateAudit = new Audit();
        updateAudit.setId(audit.getId());
        updateAudit.setStatus(AuditStatus.REVOKED.getCode());
        auditService.updateById(updateAudit);
        log.info("批次状态已更新为已撤销，批次编号: {}", auditNo);

        // 5. 更新所有项目状态为已撤销
        auditItemService.updateItemStatusByAuditId(audit.getId(), AuditStatus.REVOKED.getCode());
        log.info("所有审核项目状态已更新为已撤销，批次编号: {}", auditNo);

        log.info("审核申请撤销完成，批次编号: {}", auditNo);
    }
}
