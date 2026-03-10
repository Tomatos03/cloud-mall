package com.onlineshop.framework.models.audit.application;

import com.onlineshop.framework.models.audit.dto.AuditStatusDTO;
import com.onlineshop.framework.models.audit.dto.AuditDecisionDTO;

/**
 * 审核应用服务接口
 * 定义审核业务操作的统一API
 * 
 * 重构说明：
 * 1. 提交操作已由具体审核器直接处理，无需通过 AppService
 * 2. 本服务仅处理审核决策的分发（路由到对应的审核器）
 * 3. 查询操作保留，用于获取审核相关数据
 *
 * @author Tomatos
 * @date 2026/3/7
 */
public interface IAuditAppService {
    
    /**
     * 批量处理审核决策
     * 处理一个审核批次内所有项的决策，原子性执行
     * <p>
     * 流程：
     * 1. 前端在审核详情页逐个决定每个 audit_item
     * 2. 点击"提交决策"，一次性发送所有决策
     * 3. 后端验证该批次的所有项都有决策
     * 4. 根据 audit.bizType 获取对应的 Auditor
     * 5. 调用 auditor.handleBatchDecisions() 批量处理，原子性执行
     *
     * @param batchDecision 包含 auditId 和该批次内所有项的决策列表
     * @param type
     * @throws com.onlineshop.framework.exception.BizException 当验证失败或业务异常时抛出
     */
    void submitAuditDecisions(AuditDecisionDTO batchDecision, String type);
    
    /**
     * 查询当前用户的创建店铺审核状态
     * 
     * @return 审核状态 DTO
     */
    AuditStatusDTO queryUserCreateStoreAuditStatus();

    /**
     * 撤销审核申请（仅限待审核状态）
     * 撤销审核批次和其下所有项目，将其状态改为已撤销
     *
     * @param auditNo 审核批次编号
     * @throws com.onlineshop.framework.exception.BizException 当审核批次不存在或状态不符合要求时
     */
    void withdrawAudit(String auditNo);
}
