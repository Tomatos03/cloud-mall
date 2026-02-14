package com.onlineshop.framework.models.audit.application;

import com.onlineshop.framework.models.audit.enums.AuditType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 审核代理工厂
 * 负责管理和路由所有IAuditDelegate实现类
 * 根据审核类型动态获取对应的审核委托处理服务
 *
 * @author Tomatos
 * @date 2026/1/13
 */
@Component
@RequiredArgsConstructor
public class AuditDelegateFactory {
    private static volatile Map<String, IAuditDelegate> AUDIT_DELEGATE_MAP;
    private final List<IAuditDelegate> delegates;

    /**
     * 根据审核类型获取对应的审核委托处理器
     *
     * @param type 审核类型
     * @return IAuditDelegate实现类
     */
    public IAuditDelegate getDelegate(AuditType type) {
        ensureInit();
        return AUDIT_DELEGATE_MAP.get(type.getCode());
    }

    /**
     * 初始化委托处理器映射表
     * 使用双检查锁定模式确保线程安全的延迟初始化
     */
    private void ensureInit() {
        if (AUDIT_DELEGATE_MAP != null) {
            return;
        }

        synchronized (AuditDelegateFactory.class) {
            if (AUDIT_DELEGATE_MAP != null) {
                return;
            }
            AUDIT_DELEGATE_MAP = new HashMap<>();
            for (IAuditDelegate delegate : delegates) {
                AuditType supportType = delegate.getSupportAuditType();
                if (supportType != null) {
                    AUDIT_DELEGATE_MAP.put(supportType.getCode(), delegate);
                }
            }
        }
    }
}