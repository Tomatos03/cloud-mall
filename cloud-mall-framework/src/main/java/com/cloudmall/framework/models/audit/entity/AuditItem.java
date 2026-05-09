package com.cloudmall.framework.models.audit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 审核项目表
 * 
 * 设计说明：
 * - 一个Audit批次对应多个AuditItem（一对多关系）
 * - 每个AuditItem代表待审核的一个业务对象
 * - snapshot存储单个业务对象的快照JSON
 * - status: 项级状态（PENDING/APPROVED/REJECTED）
 * - 支持逐项审批和批量审批
 */
@Data
@TableName("audit_item")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditItem {
    /**
     * 项ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联批次ID
     */
    private Long auditId;

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
     * 上一条审核明细ID（用于串联审核版本）
     */
    private Long prevItemId;

    /**
     * 是否为最新审核记录: 1=是, 0=否
     */
    private Integer isLatest;
}
