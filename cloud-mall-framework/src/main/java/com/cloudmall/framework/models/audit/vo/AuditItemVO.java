package com.cloudmall.framework.models.audit.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审核项目VO
 * 代表Audit批次下的单个审核项目
 *
 * @author : Tomatos
 * @date : 2026/3/7
 */
@Data
public class AuditItemVO {
    /**
     * 项目ID
     */
    private Long id;

    /**
     * 业务对象ID（如商品ID、活动ID等）
     */
    private Long bizId;

    /**
     * 项目状态: PENDING-待审核, APPROVED-已通过, REJECTED-已拒绝
     */
    private String status;

    /**
     * 审批意见/拒绝原因
     */
    private String reason;

    /**
     * 业务对象快照（JSON格式）
     * 存储该项对应的业务对象的完整信息
     */
    private String snapshot;

    /**
     * 审批人ID
     */
    private Long auditorId;

    /**
     * 审批人姓名
     */
    private String auditorName;

    /**
     * 审批时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;
}
