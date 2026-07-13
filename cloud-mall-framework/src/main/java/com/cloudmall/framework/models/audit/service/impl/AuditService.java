package com.cloudmall.framework.models.audit.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudmall.framework.common.enums.BizErrorCode;
import com.cloudmall.framework.models.audit.dto.AuditParamsDTO;
import com.cloudmall.framework.models.audit.entity.Audit;
import com.cloudmall.framework.models.audit.entity.AuditItem;
import com.cloudmall.framework.models.audit.enums.AuditBizType;
import com.cloudmall.framework.models.audit.enums.AuditItemStatus;
import com.cloudmall.framework.models.audit.enums.AuditStatus;
import com.cloudmall.framework.models.audit.mapper.AuditMapper;
import com.cloudmall.framework.models.audit.service.IAuditItemService;
import com.cloudmall.framework.models.audit.service.IAuditService;
import com.cloudmall.framework.models.audit.vo.AuditItemVO;
import com.cloudmall.framework.models.audit.vo.AuditListItemVO;
import com.cloudmall.framework.utils.AssertUtils;
import com.cloudmall.framework.context.AuthUserContext;
import com.cloudmall.framework.utils.IDNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审核批次服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService extends ServiceImpl<AuditMapper, Audit> implements IAuditService {
    @Autowired
    private IAuditItemService auditItemService;

    // ==================== 查询方法 ====================

    @Override
    public IPage<AuditListItemVO> pageQuery(AuditParamsDTO queryDTO) {
        log.info("分页查询审核批次，页码: {}, 每页数量: {}", queryDTO.getPage(), queryDTO.getPageSize());
        LambdaQueryWrapper<Audit> wrapper = buildQueryWrapper(queryDTO);
        IPage<Audit> page = this.page(new Page<>(queryDTO.getPage(), queryDTO.getPageSize()), wrapper);
        return page.convert(this::convertToAuditListItemVO);
    }

    @Override
    public List<AuditItemVO> getAuditById(Long auditId) {
        log.info("查询审核批次详情，批次ID: {}", auditId);
        Audit audit = this.getById(auditId);
        AssertUtils.notNull(audit, BizErrorCode.AUDIT_NOT_EXIST);
        return auditItemService.getAuditById(auditId);
    }

    @Override
    public List<AuditItemVO> getAuditByNo(String auditNo) {
        log.info("查询审核批次详情，批次编号: {}", auditNo);
        Audit audit = this.lambdaQuery()
                          .eq(Audit::getAuditNo, auditNo)
                          .one();
        AssertUtils.notNull(audit, BizErrorCode.AUDIT_NOT_EXIST);
        return auditItemService.getAuditById(audit.getId());
    }

    @Override
    public Audit createAuditBatch(String bizType, Long bizPid, int itemCount) {
        Audit audit = Audit.builder()
                           .auditNo(IDNumber.generateAuditNo())
                           .bizType(bizType)
                           .bizPid(bizPid)
                           .status(AuditStatus.PENDING.getCode())
                           .totalCount(itemCount)
                           .approvedCount(0)
                           .rejectedCount(0)
                           .applicantId(AuthUserContext.getUserId())
                           .applicantName(AuthUserContext.getUsername())
                           .createTime(LocalDateTime.now())
                           .build();
        save(audit);
        return audit;
    }

    // ==================== 状态推算 ====================

    @Override
    public void recalculateAuditStatus(Long auditId) {
        Audit audit = getById(auditId);
        AssertUtils.notNull(audit, BizErrorCode.AUDIT_LOG_NOT_EXISTS);

        List<AuditItem> items = auditItemService.queryAuditItems(auditId);
        AssertUtils.notEmpty(items, BizErrorCode.AUDIT_LOG_NOT_EXISTS);

        int totalCount = items.size();
        int approvedCount = (int) items.stream()
                                       .filter(i -> AuditItemStatus.APPROVED.getCode()
                                                                            .equals(i.getStatus()))
                                       .count();
        int rejectedCount = (int) items.stream()
                                       .filter(i -> AuditItemStatus.REJECTED.getCode()
                                                                            .equals(i.getStatus()))
                                       .count();

        // 更新统计信息
        audit.setTotalCount(totalCount);
        audit.setApprovedCount(approvedCount);
        audit.setRejectedCount(rejectedCount);

        // 状态推算（仅在所有项都审批完时才推算）
        if (approvedCount + rejectedCount == totalCount) {
            if (rejectedCount == 0) {
                audit.setStatus(AuditStatus.APPROVED.getCode());
                log.info("批次 {} 所有项都通过，状态更新为 APPROVED", auditId);
            } else if (approvedCount == 0) {
                audit.setStatus(AuditStatus.REJECTED.getCode());
                log.info("批次 {} 所有项都拒绝，状态更新为 REJECTED", auditId);
            } else {
                audit.setStatus(AuditStatus.PARTIAL.getCode());
                log.info("批次 {} 部分通过部分拒绝，状态更新为 PARTIAL", auditId);
            }
        }
        updateById(audit);
    }

    // ==================== 历史查询 ====================

    @Override
    public Audit queryLatestAudit(AuditBizType type, Long targetId) {
        return this.lambdaQuery()
                   .eq(Audit::getBizType, type.getCode())
                   .orderByDesc(Audit::getId)
                   .last("LIMIT 1")
                   .one();
    }

    // ==================== 辅助方法 ====================

    /**
     * 构建分页查询条件
     */
    private LambdaQueryWrapper<Audit> buildQueryWrapper(AuditParamsDTO queryDTO) {
        LambdaQueryWrapper<Audit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getStatus() != null, Audit::getStatus, queryDTO.getStatus());
        wrapper.eq(queryDTO.getApplicantId() != null, Audit::getApplicantId, queryDTO.getApplicantId());
        wrapper.eq(queryDTO.getBizPid() != null, Audit::getBizPid, queryDTO.getBizPid());
        wrapper.orderByDesc(Audit::getCreateTime);
        return wrapper;
    }

    private AuditListItemVO convertToAuditListItemVO(Audit audit) {
        return BeanUtil.copyProperties(audit, AuditListItemVO.class);
    }
}
