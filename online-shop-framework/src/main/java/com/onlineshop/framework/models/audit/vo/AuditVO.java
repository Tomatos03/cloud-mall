package com.onlineshop.framework.models.audit.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 审核日志视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditVO {
    /**
     * 审核记录ID
     */
    private Long auditId;

    /**
     * 被审核对象类型: GOODS / SKU / OTHER
     */
    private String targetType;

    /**
     * 被审核对象ID
     */
    private Long targetId;

    /**
     * 审核状态: PENDING-待审核, APPROVED-通过, REJECTED-拒绝, REVOKED-已撤销
     */
    private String status;

    /**
     * 审核状态名称
     */
    private String statusName;

    /**
     * 审核备注/拒绝原因
     */
    private String reason;

    /**
     * 申请人ID
     */
    private Long applicantId;

    /**
     * 申请人姓名
     */
    private String applicantName;

    /**
     * 审核人ID
     */
    private Long auditorId;

    /**
     * 审核人姓名
     */
    private String auditorName;

    /**
     * 扩展信息: 可存储SKU组合/商品规格等JSON
     */
    private String extraInfo;

    /**
     * 申请时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 审核时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;
}