package com.onlineshop.framework.models.audit.dto;

import com.onlineshop.framework.common.entity.PageParamsDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 审核查询条件DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AuditParamsDTO extends PageParamsDTO {
    /**
     * 被审核对象类型
     */
    private String targetType;

    /**
     * 被审核对象ID
     */
    private Long targetId;

    private String status;

    /**
     * 申请人ID
     */
    private Long applicantId;

    /**
     * 审核人ID
     */
    private Long auditorId;
}