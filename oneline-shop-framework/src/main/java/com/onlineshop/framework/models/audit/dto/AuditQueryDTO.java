package com.onlineshop.framework.models.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 审核查询条件DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditQueryDTO {
    /**
     * 当前页码（从1开始）
     */
    private Long pageNo;

    /**
     * 每页数量
     */
    private Long pageSize;

    /**
     * 被审核对象类型
     */
    private String targetType;

    /**
     * 被审核对象ID
     */
    private Long targetId;

    /**
     * 审核状态
     */
    private Byte status;

    /**
     * 申请人ID
     */
    private Long applicantId;

    /**
     * 审核人ID
     */
    private Long auditorId;
}