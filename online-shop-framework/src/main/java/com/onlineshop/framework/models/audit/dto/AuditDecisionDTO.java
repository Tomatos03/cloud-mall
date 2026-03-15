package com.onlineshop.framework.models.audit.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 批量审核决策DTO
 * 
 * 用于前端一次提交一个审核批次的所有项的决策
 * 
 * 流程：
 * 1. 管理员进入审核批次详情页
 * 2. 查看所有 audit_item 列表
 * 3. 逐个决定每个 item 是批准还是拒绝
 * 4. 点击"提交决策"，发送本 DTO
 * 5. 后端批量处理所有决策，原子性执行（全成功或全失败）
 */
@Data
public class AuditDecisionDTO {
    /**
     * 审核批次ID
     */
    @NotNull(message = "审核批次ID不能为空")
    private String auditNo;

    /**
     * 该批次内所有项的决策列表
     */
    @NotEmpty(message = "决策列表不能为空")
    @Valid
    private List<AuditItemDecision> decisions;


    @Data
    public static class AuditItemDecision {
        /**
         * 审核项ID
         */
        @NotNull(message = "审核项ID不能为空")
        private Long auditItemId;

        /**
         * 决策：true=通过，false=拒绝
         */
        @NotNull(message = "决策结果不能为空")
        private Boolean approved;

        /**
         * 拒绝原因（仅在拒绝时需要）
         */
        private String reason;
    }
}
