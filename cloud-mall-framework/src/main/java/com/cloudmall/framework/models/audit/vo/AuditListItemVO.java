package com.cloudmall.framework.models.audit.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/3/7
 */
@Data
public class AuditListItemVO {
    private String auditNo;

    /** 业务类型 */
    private String bizType;

    /** 批次状态 */
    private String status;

    /** 总项目数 */
    private Integer totalCount;

    /** 已通过数 */
    private Integer approvedCount;

    /** 已拒绝数 */
    private Integer rejectedCount;

    /** 申请人ID（Long转String） */
    private Long applicantId;

    /** 申请人名称 */
    private String applicantName;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
