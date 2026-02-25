package com.onlineshop.framework.models.audit.application;

import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.exception.BizException;
import com.onlineshop.framework.models.audit.enums.AuditType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Tomatos
 * @date 2026/2/26
 */
@Component
@RequiredArgsConstructor
public class AuditorFactory {
    private final List<AbstractAuditor<?>> auditors;
    
    // 缓存 Map：AuditType code → Auditor 实例
    private volatile Map<String, AbstractAuditor<?>> auditorCache;

    /**
     * 根据审核类型获取对应的 Auditor 实例
     * 
     * @param type 审核类型（来自 AuditType 枚举的 code 值）
     * @return 对应的 Auditor 实例
     * @throws BizException 当审核类型未知或无法匹配到 Auditor 时抛出异常
     */
    public AbstractAuditor<?> getAuditor(String type) {
        // 验证审核类型是否合法（通过 AuditType.of() 验证）
        AuditType auditType = AuditType.of(type);
        
        // 第一次检查：避免重复初始化（无锁）
        if (auditorCache == null) {
            synchronized (this) {
                // 第二次检查：在锁内再检查一次，避免竞态条件
                if (auditorCache == null) {
                    initAuditorCache();
                }
            }
        }
        
        AbstractAuditor<?> auditor = auditorCache.get(type);
        if (auditor == null) {
            throw new BizException(BizErrorCode.UNSUPPORTED_AUDIT_TYPE);
        }
        
        return auditor;
    }

    /**
     * 初始化 Auditor 缓存 Map
     * 
     * 算法原理：
     * 1. 遍历 AuditType 枚举中的所有类型
     * 2. 对每个类型，遍历所有注入的 Auditor
     * 3. 调用 Auditor 的 support(auditType) 方法
     * 4. 如果返回 true，则将该 Auditor 放入缓存 Map
     * 
     * 优势：
     * - 完全由 Auditor 声明自己支持的类型
     * - 避免了硬编码的类名匹配逻辑
     * - 支持一个 Auditor 支持多种类型（虽然当前设计一对一）
     */
    private void initAuditorCache() {
        Map<String, AbstractAuditor<?>> cache = new ConcurrentHashMap<>();
        
        // 遍历枚举中的所有审核类型
        for (AuditType auditType : AuditType.values()) {
            // 遍历所有注入的 Auditor
            for (AbstractAuditor<?> auditor : auditors) {
                // 询问 Auditor 是否支持此类型
                if (auditor.support(auditType)) {
                    cache.put(auditType.getCode(), auditor);
                    break;  // 找到支持此类型的 Auditor，结束内层循环
                }
            }
        }

        auditorCache = cache;
    }
}
