package com.onlineshop.framework.models.audit.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.exception.BizException;
import com.onlineshop.framework.models.audit.dto.AuditDecisionDTO;
import com.onlineshop.framework.models.audit.dto.AuditParamsDTO;
import com.onlineshop.framework.models.audit.dto.AuditSubmit;
import com.onlineshop.framework.models.audit.entity.Audit;
import com.onlineshop.framework.models.audit.enums.AuditStatus;
import com.onlineshop.framework.models.audit.enums.AuditType;
import com.onlineshop.framework.models.audit.mapper.AuditMapper;
import com.onlineshop.framework.models.audit.service.IAuditService;
import com.onlineshop.framework.models.audit.vo.AuditVO;
import com.onlineshop.framework.utils.AssertUtils;
import com.onlineshop.framework.utils.AuthUserUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 审核日志服务实现
 */
@Service
@RequiredArgsConstructor
public class AuditService extends ServiceImpl<AuditMapper, Audit> implements IAuditService {
    @Override
    public void submitAudit(AuditSubmit submitDTO) {
        validateSubmitDTO(submitDTO);
        Audit audit = buildNewAudit(submitDTO, AuthUserUtils.getUserId(), AuthUserUtils.getUsername());
        save(audit);
    }

    @Override
    public IPage<AuditVO> pageQuery(AuditParamsDTO queryDTO) {
        LambdaQueryWrapper<Audit> wrapper = buildQueryWrapper(queryDTO);
        IPage<Audit> page = this.page(new Page<>(queryDTO.getPageNo(), queryDTO.getPageSize()), wrapper);
        return page.convert(this::convertToVO);
    }

    @Override
    public AuditVO getAuditById(Long auditId) {
        Audit audit = this.getById(auditId);
        AssertUtils.notNull(audit, BizErrorCode.AUDIT_LOG_NOT_EXISTS);
        return convertToVO(audit);
    }

    @Override
    public boolean withdrawAudit(Long auditId) {
        Audit audit = this.getById(auditId);
        AssertUtils.notNull(audit, BizErrorCode.AUDIT_LOG_NOT_EXISTS);

        validateWithdrawPermission(audit);
        AssertUtils.isEqual(audit.getStatus(), AuditStatus.PENDING.getCode(), BizErrorCode.AUDIT_LOG_NOT_EXISTS);

        Audit updateLog = Audit.builder()
                               .id(auditId)
                               .status(AuditStatus.REVOKED.getCode())
                               .build();

        return this.updateById(updateLog);
    }

    @Override
    public Audit queryLatestAudit(AuditType type, Long targetId) {
        return this.lambdaQuery()
                   .eq(Audit::getTargetType, type.getCode())
                   .eq(Audit::getTargetId, targetId)
                   .orderByDesc(Audit::getId)
                   .last("LIMIT 1")
                   .one();
    }

    @Override
    public List<Audit> queryLatestAuditByTypeBatch(AuditType type, Collection<? extends Serializable> targetIds) {
        if (CollectionUtil.isEmpty(targetIds)) {
            return Collections.emptyList();
        }

        return this.lambdaQuery()
                   .eq(Audit::getTargetType, type.getCode())
                   .in(Audit::getTargetId, targetIds)
                   .orderByDesc(Audit::getId)
                   .list();
    }

    @Override
    public void updateAudit(Audit audit) {
        this.updateById(audit);
    }

    /**
     * 验证撤回权限（仅申请人可撤回）
     */
    private void validateWithdrawPermission(Audit audit) {
        Long currentUserId = AuthUserUtils.getUserId();
        if (!Objects.equals(currentUserId, audit.getApplicantId())) {
            throw new BizException(BizErrorCode.AUDIT_WITHDRAW_OWN_ONLY);
        }
    }

    /**
     * 构建分页查询条件
     */
    private LambdaQueryWrapper<Audit> buildQueryWrapper(AuditParamsDTO queryDTO) {
        LambdaQueryWrapper<Audit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getTargetType() != null, Audit::getTargetType, queryDTO.getTargetType());
        wrapper.eq(queryDTO.getTargetId() != null, Audit::getTargetId, queryDTO.getTargetId());
        wrapper.in(queryDTO.getStatus() != null, Audit::getStatus, queryDTO.getStatus());
        wrapper.eq(queryDTO.getApplicantId() != null, Audit::getApplicantId, queryDTO.getApplicantId());
        wrapper.orderByDesc(Audit::getCreateTime);
        return wrapper;
    }

    /**
     * 将Audit转换为AuditLogVO
     */
    private AuditVO convertToVO(@NonNull Audit audit) {
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
                      .snapshot(audit.getSnapshot())
                      .createTime(audit.getCreateTime())
                      .auditTime(audit.getAuditTime())
                      .build();
    }

    /**
     * 验证提交审核DTO
     */
    private void validateSubmitDTO(AuditSubmit submitDTO) {
        if (submitDTO.getTargetType() == null || submitDTO.getTargetId() == null) {
            throw new BizException(BizErrorCode.AUDIT_SUBMIT_PARAMS_INCOMPLETE);
        }
    }

    /**
     * 从提交DTO构建审核记录
     */
    private Audit buildNewAudit(AuditSubmit submitDTO, Long applicantId, String applicantName) {
        return Audit.builder()
                    .targetType(submitDTO.getTargetType())
                    .targetId(submitDTO.getTargetId())
                    .status(AuditStatus.PENDING.getCode())
                    .applicantId(applicantId)
                    .applicantName(applicantName)
                    .snapshot(submitDTO.getSnapShot())
                    .createTime(LocalDateTime.now())
                    .build();
    }

    public void updateAudit(AuditDecisionDTO decisionDTO, Long targetId) {
        this.updateById(buildAudit(decisionDTO, targetId));
    }

    @Override
    public AuditStatus queryAuditStatus(AuditType type, Long targetId) {
        Audit audit = queryLatestAudit(type, targetId);
        if (audit == null) {
            return null;
        }
        return AuditStatus.of(audit.getStatus());
    }

    private Audit buildAudit(AuditDecisionDTO decisionDTO, Long targetId) {
        Audit updateLog = Audit.builder()
                               .id(decisionDTO.getAuditId())
                               .status(
                                       decisionDTO.getApproved()
                                               ? AuditStatus.APPROVED.getCode()
                                               : AuditStatus.REJECTED.getCode()
                               )
                               .reason(decisionDTO.getReason())
                               .auditorId(AuthUserUtils.getUserId())
                               .auditorName(AuthUserUtils.getUsername())
                               .auditTime(LocalDateTime.now())
                               .build();

        // 如果审核通过且targetId不为空，更新targetId
        if (decisionDTO.getApproved() && targetId != null) {
            updateLog.setTargetId(targetId);
        }
        return updateLog;
    }
}
