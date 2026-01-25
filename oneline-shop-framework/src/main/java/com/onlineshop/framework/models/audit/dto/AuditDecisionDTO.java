package com.onlineshop.framework.models.audit.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 审核结果DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditDecisionDTO {
    /**
     * 审核日志ID
     */
    @NotNull
    private Long auditId;

    /**
     * 审核结果: true-通过, false-拒绝
     */
    private Boolean approved;

    /**
     * 审核备注/拒绝原因
     */
    private String reason;
}