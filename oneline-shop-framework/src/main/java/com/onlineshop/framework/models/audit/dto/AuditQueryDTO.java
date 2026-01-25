package com.onlineshop.framework.models.audit.dto;

import com.onlineshop.framework.common.entity.PageQueryDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 审核查询条件DTO
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AuditQueryDTO extends PageQueryDTO {
    /**
     * 被审核对象类型
     */
    private String targetType;

    /**
     * 被审核对象ID
     */
    private Long targetId;

    /**
     * 审核状态: 0-待审核, 1-通过, 2-拒绝, 3-已撤销
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