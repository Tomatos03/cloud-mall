package com.onlineshop.framework.models.audit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.exception.BusinessException;
import com.onlineshop.framework.models.audit.dto.AuditDecisionDTO;
import com.onlineshop.framework.models.audit.dto.AuditQueryDTO;
import com.onlineshop.framework.models.audit.dto.AuditSubmitDTO;
import com.onlineshop.framework.models.audit.entity.Audit;
import com.onlineshop.framework.models.audit.enums.AuditStatus;
import com.onlineshop.framework.models.audit.mapper.AuditMapper;
import com.onlineshop.framework.models.audit.service.IAuditService;
import com.onlineshop.framework.models.audit.vo.AuditVO;
import com.onlineshop.framework.utils.context.UserContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 审核日志服务实现
 */
@Service
@RequiredArgsConstructor
public class AuditService extends ServiceImpl<AuditMapper, Audit> implements IAuditService {
    @Override
    public void submitAudit(AuditSubmitDTO submitDTO) {
        validateSubmitDTO(submitDTO);
        Long applicantId = UserContextHolder.getUserId();
        String applicantName = UserContextHolder.getUserName();

        Audit audit = buildAuditFromSubmitDTO(submitDTO, applicantId, applicantName);
        saveAudit(audit);
    }

    @Override
    public void auditDecision(AuditDecisionDTO decisionDTO) {
        validateDecisionDTO(decisionDTO);

        Audit audit = getAndValidateAudit(decisionDTO.getAuditId());
        validateAuditPending(audit);

        Long auditorId = UserContextHolder.getUserId();
        String auditorName = UserContextHolder.getUserName();

        Audit updateLog = buildUpdateAuditFromDecision(audit, decisionDTO, auditorId, auditorName);
        this.updateById(updateLog);
    }

    @Override
    public IPage<AuditVO> pageQuery(AuditQueryDTO queryDTO) {
        LambdaQueryWrapper<Audit> wrapper = buildQueryWrapper(queryDTO);
        IPage<Audit> page = this.page(new Page<>(queryDTO.getPageNo(), queryDTO.getPageSize()), wrapper);

        return page.convert(this::convertToVO);
    }

    @Override
    public IPage<AuditVO> pageQueryMerchant(AuditQueryDTO queryDTO) {
        LambdaQueryWrapper<Audit> wrapper = buildMerchantQueryWrapper(queryDTO);
        IPage<Audit> page = this.page(new Page<>(queryDTO.getPageNo(), queryDTO.getPageSize()), wrapper);
        return page.convert(this::convertToVO);
    }

    @Override
    public AuditVO getAuditById(Long auditId) {
        Audit audit = this.getById(auditId);
        if (audit == null) {
            throw new BusinessException(BizErrorCode.AUDIT_LOG_NOT_EXISTS);
        }

        return convertToVO(audit);
    }

    @Override
    public boolean withdrawAudit(Long auditId) {
        Audit audit = this.getById(auditId);
        if (audit == null) {
            throw new BusinessException(BizErrorCode.AUDIT_LOG_NOT_EXISTS);
        }

        validateWithdrawPermission(audit);
        validateAuditPending(audit);

        Audit updateLog = Audit.builder()
                               .id(auditId)
                               .status(AuditStatus.REVOKED.getCode())
                               .build();

        return this.updateById(updateLog);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 验证提交审核DTO
     */
    private void validateSubmitDTO(AuditSubmitDTO submitDTO) {
        if (submitDTO.getTargetType() == null || submitDTO.getTargetId() == null) {
            throw new BusinessException(BizErrorCode.AUDIT_SUBMIT_PARAMS_INCOMPLETE);
        }
    }

    /**
     * 验证审核决定DTO
     */
    private void validateDecisionDTO(AuditDecisionDTO decisionDTO) {
        if (decisionDTO.getAuditId() == null || decisionDTO.getApproved() == null) {
            throw new BusinessException(BizErrorCode.AUDIT_DECISION_PARAMS_INCOMPLETE);
        }
    }

    /**
     * 获取审核记录并验证存在性
     */
    private Audit getAndValidateAudit(Long auditId) {
        Audit audit = this.getById(auditId);
        if (audit == null) {
            throw new BusinessException(BizErrorCode.AUDIT_LOG_NOT_EXISTS);
        }
        return audit;
    }

    /**
     * 验证审核状态为待审核
     */
    private void validateAuditPending(Audit audit) {
        if (audit.getStatus() != AuditStatus.PENDING.getCode()) {
            throw new BusinessException(BizErrorCode.AUDIT_ONLY_PENDING);
        }
    }

    /**
     * 验证撤回权限（仅申请人可撤回）
     */
    private void validateWithdrawPermission(Audit audit) {
        Long currentUserId = UserContextHolder.getUserId();
        if (!Objects.equals(currentUserId, audit.getApplicantId())) {
            throw new BusinessException(BizErrorCode.AUDIT_WITHDRAW_OWN_ONLY);
        }
    }

    /**
     * 从提交DTO构建审核记录
     */
    private Audit buildAuditFromSubmitDTO(AuditSubmitDTO submitDTO, Long applicantId, String applicantName) {
        return Audit.builder()
                    .targetType(submitDTO.getTargetType())
                    .targetId(submitDTO.getTargetId())
                    .status(AuditStatus.PENDING.getCode())
                    .applicantId(applicantId)
                    .applicantName(applicantName)
                    .extraInfo(submitDTO.getExtraInfo())
                    .createTime(LocalDateTime.now())
                    .build();
    }

    /**
     * 从审核决定DTO构建审核记录更新对象
     */
    private Audit buildUpdateAuditFromDecision(Audit audit, AuditDecisionDTO decisionDTO, 
                                                Long auditorId, String auditorName) {
        return Audit.builder()
                    .id(audit.getId())
                    .status(decisionDTO.getApproved() ? AuditStatus.APPROVED.getCode() : AuditStatus.REJECTED.getCode())
                    .reason(decisionDTO.getReason())
                    .auditorId(auditorId)
                    .auditorName(auditorName)
                    .auditTime(LocalDateTime.now())
                    .build();
    }

    /**
     * 保存审核记录
     */
    private void saveAudit(Audit audit) {
        boolean success = this.save(audit);
        if (!success) {
            throw new BusinessException(BizErrorCode.AUDIT_SUBMIT_FAILED);
        }
    }

    /**
     * 构建分页查询条件
     */
    private LambdaQueryWrapper<Audit> buildQueryWrapper(AuditQueryDTO queryDTO) {
        LambdaQueryWrapper<Audit> wrapper = new LambdaQueryWrapper<>();
        if (queryDTO.getTargetType() != null) {
            wrapper.eq(Audit::getTargetType, queryDTO.getTargetType());
        }
        applyCommonConditions(wrapper, queryDTO);
        return wrapper;
    }

    /**
     * 构建商家分页查询条件（过滤店铺下的商品审核记录）
     * 仅返回 targetType=GOODS 且 targetId 对应的商品属于该店铺的审核记录
     */
    private LambdaQueryWrapper<Audit> buildMerchantQueryWrapper(AuditQueryDTO queryDTO) {
        queryDTO.setApplicantId(UserContextHolder.getUserId());

        LambdaQueryWrapper<Audit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Audit::getTargetType, "GOODS");
        applyCommonConditions(wrapper, queryDTO);
        return wrapper;
    }

    /**
     * 应用公共查询条件
     * 包含：审核状态、申请人ID、审核人ID、排序
     */
    private void applyCommonConditions(LambdaQueryWrapper<Audit> wrapper, AuditQueryDTO queryDTO) {
        if (queryDTO.getTargetId() != null) {
            wrapper.eq(Audit::getTargetId, queryDTO.getTargetId());
        }

        if (queryDTO.getStatus() != null) {
            wrapper.eq(Audit::getStatus, queryDTO.getStatus());
        }

        if (queryDTO.getApplicantId() != null) {
            wrapper.eq(Audit::getApplicantId, queryDTO.getApplicantId());
        }
        if (queryDTO.getAuditorId() != null) {
            wrapper.eq(Audit::getAuditorId, queryDTO.getAuditorId());
        }

        wrapper.orderByDesc(Audit::getCreateTime);
    }

    /**
     * 将Audit转换为AuditLogVO
     */
    private AuditVO convertToVO(Audit audit) {
        if (audit == null) {
            return null;
        }

        AuditStatus status = AuditStatus.of(audit.getStatus());
        String statusName = status != null ? status.getName() : "未知";

        return AuditVO.builder()
                      .auditId(audit.getId())
                      .targetType(audit.getTargetType())
                      .targetId(audit.getTargetId())
                      .status(audit.getStatus())
                      .statusName(statusName)
                      .reason(audit.getReason())
                      .applicantId(audit.getApplicantId())
                      .applicantName(audit.getApplicantName())
                      .auditorId(audit.getAuditorId())
                      .auditorName(audit.getAuditorName())
                      .extraInfo(audit.getExtraInfo())
                      .createTime(audit.getCreateTime())
                      .auditTime(audit.getAuditTime())
                      .build();
    }
}