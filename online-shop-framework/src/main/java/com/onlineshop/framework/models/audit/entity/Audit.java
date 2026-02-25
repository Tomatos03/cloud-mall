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
 * 审核日志实体类
 * 
 * 设计说明：
 * - snapshot字段存储审核请求的完整JSON序列化
 * - 通过targetType可以确定具体的AuditRequest类型
 * - 审核决策时可以通过snapshot恢复出原始的审核请求对象
 */
@Data
@TableName("audit")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Audit {
    /**
     * 审核记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 被审核对象类型: GOODS / STORE_REGISTER / SECKILL_ACTIVITY
     * 此字段用于标识审核请求的业务类型，Auditor工厂使用此字段获取对应的处理器
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
     * 审核请求快照 - JSON格式
     *
     */
    private String snapshot;

    /**
     * 申请时间
     */
    private LocalDateTime createTime;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;
}