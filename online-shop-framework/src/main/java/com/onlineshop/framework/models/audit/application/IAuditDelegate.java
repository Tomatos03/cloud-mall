package com.onlineshop.framework.models.audit.application;

import com.onlineshop.framework.models.audit.entity.Audit;
import com.onlineshop.framework.models.audit.enums.AuditType;

/**
 * 审核处理策略接口
 * 定义审核相关的处理操作，通过策略模式实现不同审核对象类型的处理逻辑解耦
 * 使用泛型避免类型强转，提高类型安全性
 *
 * @author Tomatos
 * @date 2026/1/13
 */
public interface IAuditDelegate {

    /**
     * 获取该委托处理器支持的审核类型
     * 工厂在初始化时会调用此方法来确定该处理器可以处理哪种类型的审核
     *
     * @return 支持的审核类型
     */
    AuditType getSupportAuditType();

    /**
     * 提交审核
     * 将需要审核的对象信息序列化后提交到审核系统
     *
     * @param payload 业务对象的请求体（泛型类型T）
     * @param targetId 目标对象ID（新增为null或0）
     */
    void submitAudit(Object payload);

    /**
     * 处理审核通过的回调
     * 当审核记录被通过时调用，执行实际的业务操作（如保存商品）
     *
     * @param audit 审核记录信息
     */
    void onAuditApproved(Audit audit);

    /**
     * 处理审核拒绝的回调
     * 当审核记录被拒绝时调用，可执行清理或记录操作
     *
     * @param audit 审核记录信息
     * @param reason 拒绝原因
     */
    void onAuditRejected(Audit audit, String reason);
}