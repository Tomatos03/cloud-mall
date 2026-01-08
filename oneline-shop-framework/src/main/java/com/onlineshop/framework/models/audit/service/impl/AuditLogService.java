package com.onlineshop.framework.models.audit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineshop.framework.enums.BizErrorCode;
import com.onlineshop.framework.exception.BusinessException;
import com.onlineshop.framework.models.audit.dto.AuditDecisionDTO;
import com.onlineshop.framework.models.audit.dto.AuditQueryDTO;
import com.onlineshop.framework.models.audit.dto.AuditSubmitDTO;
import com.onlineshop.framework.models.audit.entity.AuditLog;
import com.onlineshop.framework.models.audit.enums.AuditStatus;
import com.onlineshop.framework.models.audit.mapper.AuditLogMapper;
import com.onlineshop.framework.models.audit.service.IAuditLogService;
import com.onlineshop.framework.models.audit.vo.AuditLogVO;
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
public class AuditLogService extends ServiceImpl<AuditLogMapper, AuditLog> implements IAuditLogService {

    private final AuditLogMapper auditLogMapper;

    @Override
    public Long submitAudit(AuditSubmitDTO submitDTO) {
        // 校验参数
        if (submitDTO == null || submitDTO.getTargetType() == null || submitDTO.getTargetId() == null) {
            throw new BusinessException(BizErrorCode.AUDIT_SUBMIT_PARAMS_INCOMPLETE);
        }

        // 获取当前登录用户信息
        Long applicantId = UserContextHolder.getUserId();
        String applicantName = UserContextHolder.getUserName();

        if (applicantId == null) {
            throw new BusinessException(BizErrorCode.USER_NOT_LOGGED_IN_FOR_AUDIT_SUBMIT);
        }

        // 创建审核记录
        AuditLog auditLog = AuditLog.builder()
                .targetType(submitDTO.getTargetType())
                .targetId(submitDTO.getTargetId())
                .status(AuditStatus.PENDING.getCode())
                .applicantId(applicantId)
                .applicantName(applicantName)
                .extraInfo(submitDTO.getExtraInfo())
                .createTime(LocalDateTime.now())
                .build();

        // 保存到数据库
        boolean success = this.save(auditLog);
        if (!success) {
            throw new BusinessException(BizErrorCode.AUDIT_SUBMIT_FAILED);
        }

        return auditLog.getId();
    }

    @Override
    public boolean auditDecision(AuditDecisionDTO decisionDTO) {
        // 校验参数
        if (decisionDTO == null || decisionDTO.getAuditLogId() == null || decisionDTO.getApproved() == null) {
            throw new BusinessException(BizErrorCode.AUDIT_DECISION_PARAMS_INCOMPLETE);
        }

        // 获取审核记录
        AuditLog auditLog = this.getById(decisionDTO.getAuditLogId());
        if (auditLog == null) {
            throw new BusinessException(BizErrorCode.AUDIT_LOG_NOT_EXISTS);
        }

        // 验证审核状态
        if (auditLog.getStatus() != AuditStatus.PENDING.getCode()) {
            throw new BusinessException(BizErrorCode.AUDIT_ONLY_PENDING);
        }

        // 获取审核人信息
        Long auditorId = UserContextHolder.getUserId();
        String auditorName = UserContextHolder.getUserName();

        if (auditorId == null) {
            throw new BusinessException(BizErrorCode.AUDITOR_NOT_LOGGED_IN);
        }

        // 更新审核记录
        AuditLog updateLog = new AuditLog();
        updateLog.setId(auditLog.getId());
        updateLog.setStatus(decisionDTO.getApproved() ? AuditStatus.APPROVED.getCode() : AuditStatus.REJECTED.getCode());
        updateLog.setReason(decisionDTO.getReason());
        updateLog.setAuditorId(auditorId);
        updateLog.setAuditorName(auditorName);
        updateLog.setAuditTime(LocalDateTime.now());

        return this.updateById(updateLog);
    }

    @Override
    public IPage<AuditLogVO> pageQuery(AuditQueryDTO queryDTO) {
        if (queryDTO == null) {
            queryDTO = new AuditQueryDTO();
        }

        // 设置默认分页参数
        long pageNo = queryDTO.getPageNo() != null ? queryDTO.getPageNo() : 1;
        long pageSize = queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 10;

        // 构建查询条件
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();

        if (queryDTO.getTargetType() != null) {
            wrapper.eq(AuditLog::getTargetType, queryDTO.getTargetType());
        }
        if (queryDTO.getTargetId() != null) {
            wrapper.eq(AuditLog::getTargetId, queryDTO.getTargetId());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(AuditLog::getStatus, queryDTO.getStatus());
        }
        if (queryDTO.getApplicantId() != null) {
            wrapper.eq(AuditLog::getApplicantId, queryDTO.getApplicantId());
        }
        if (queryDTO.getAuditorId() != null) {
            wrapper.eq(AuditLog::getAuditorId, queryDTO.getAuditorId());
        }

        // 按创建时间倒序排列
        wrapper.orderByDesc(AuditLog::getCreateTime);

        // 执行分页查询
        IPage<AuditLog> page = this.page(new Page<>(pageNo, pageSize), wrapper);

        // 转换为VO
        return page.convert(this::convertToVO);
    }

    @Override
    public AuditLogVO getAuditById(Long auditId) {
        if (auditId == null) {
            throw new BusinessException(BizErrorCode.AUDIT_ID_CANNOT_BE_NULL);
        }

        AuditLog auditLog = this.getById(auditId);
        if (auditLog == null) {
            throw new BusinessException(BizErrorCode.AUDIT_LOG_NOT_EXISTS);
        }

        return convertToVO(auditLog);
    }

    @Override
    public AuditLogVO getLatestAuditByTarget(String targetType, Long targetId) {
        if (targetType == null || targetId == null) {
            return null;
        }

        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditLog::getTargetType, targetType)
                .eq(AuditLog::getTargetId, targetId)
                .orderByDesc(AuditLog::getCreateTime)
                .last("limit 1");

        AuditLog auditLog = this.getOne(wrapper);
        return auditLog == null ? null : convertToVO(auditLog);
    }

    @Override
    public long getPendingAuditCount() {
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditLog::getStatus, AuditStatus.PENDING.getCode());
        return this.count(wrapper);
    }

    @Override
    public boolean withdrawAudit(Long auditId) {
        if (auditId == null) {
            throw new BusinessException(BizErrorCode.AUDIT_ID_CANNOT_BE_NULL);
        }

        AuditLog auditLog = this.getById(auditId);
        if (auditLog == null) {
            throw new BusinessException(BizErrorCode.AUDIT_LOG_NOT_EXISTS);
        }

        // 验证当前用户是否为申请人
        Long currentUserId = UserContextHolder.getUserId();
        if (!Objects.equals(currentUserId, auditLog.getApplicantId())) {
            throw new BusinessException(BizErrorCode.AUDIT_WITHDRAW_OWN_ONLY);
        }

        // 只能撤回待审核状态的申请
        if (auditLog.getStatus() != AuditStatus.PENDING.getCode()) {
            throw new BusinessException(BizErrorCode.AUDIT_WITHDRAW_ONLY_PENDING);
        }

        AuditLog updateLog = new AuditLog();
        updateLog.setId(auditId);
        updateLog.setStatus(AuditStatus.NOT_SUBMITTED.getCode());

        return this.updateById(updateLog);
    }

    /**
     * 将AuditLog转换为AuditLogVO
     */
    private AuditLogVO convertToVO(AuditLog auditLog) {
        if (auditLog == null) {
            return null;
        }

        AuditStatus status = AuditStatus.fromCode(auditLog.getStatus());
        String statusName = status != null ? status.getName() : "未知";

        return AuditLogVO.builder()
                .id(auditLog.getId())
                .targetType(auditLog.getTargetType())
                .targetId(auditLog.getTargetId())
                .status(auditLog.getStatus())
                .statusName(statusName)
                .reason(auditLog.getReason())
                .applicantId(auditLog.getApplicantId())
                .applicantName(auditLog.getApplicantName())
                .auditorId(auditLog.getAuditorId())
                .auditorName(auditLog.getAuditorName())
                .extraInfo(auditLog.getExtraInfo())
                .createTime(auditLog.getCreateTime())
                .auditTime(auditLog.getAuditTime())
                .build();
    }
}