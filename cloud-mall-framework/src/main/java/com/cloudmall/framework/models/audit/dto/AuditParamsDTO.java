package com.cloudmall.framework.models.audit.dto;

import com.cloudmall.framework.common.entity.PageParamsDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

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
    private String bizType;
    /**
     * 业务父ID（如秒杀活动ID）
     */
    private Long bizPid;

    private String status;

    /**
     * 申请人ID
     */
    private Long applicantId;
}
