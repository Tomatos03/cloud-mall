package com.onlineshop.framework.models.audit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 审核批次表
 * 
 * 新设计说明：
 * - 改为批次模式：一个Audit对应多个AuditItem
 * - auditNo: 批次编号，唯一标识一个审核批次
 * - bizType: 业务类型（GOODS/STORE_REGISTER/SECKILL_ACTIVITY等）
 * - status: 批次状态（PENDING/APPROVED/REJECTED/PARTIAL）
 * - 统计字段：totalCount/approvedCount/rejectedCount用于追踪审批进度
 * - 不存储snapshot，所有快照在audit_item中存储
 */
@Data
@TableName("audit")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Audit {
    /**
     * 批次ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 批次编号（唯一，格式：AUD + yyyyMMddHHmmss + 6位随机数）
     */
    private String auditNo;

    /**
     * 业务类型: GOODS / STORE_REGISTER / SECKILL_ACTIVITY
     * 此字段用于标识审核请求的业务类型，Auditor工厂使用此字段获取对应的处理器
     */
    private String bizType;
    private Long bizPid;

    /**
     * 批次状态: PENDING-待审核, APPROVED-已通过, REJECTED-已拒绝, PARTIAL-部分通过
     */
    private String status;

    /**
     * 总项数
     */
    private Integer totalCount;

    /**
     * 已通过项数
     */
    private Integer approvedCount;

    /**
     * 已拒绝项数
     */
    private Integer rejectedCount;

    /**
     * 申请人ID
     */
    private Long applicantId;

    /**
     * 申请人姓名
     */
    private String applicantName;

    /**
     * 审核人ID（最后审核的人）
     */
    private Long auditorId;

    /**
     * 审核人姓名
     */
    private String auditorName;

    /**
     * 批次创建时间
     */
    private LocalDateTime createTime;

    /**
     * 最后审核时间
     */
    private LocalDateTime auditTime;
}