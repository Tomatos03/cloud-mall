package com.onlineshop.framework.models.audit.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 商品审核状态枚举
 * <p>
 * 包含：
 * PENDING("PENDING", "待审核")，APPROVED("APPROVED", "通过")，REJECTED("REJECTED", "拒绝")，REVOKED("REVOKED", "已撤销")，REAUDIT("REAUDIT", "需重新审核")
 */
@AllArgsConstructor
@Getter
public enum AuditStatus {
    /**
     * 待审核
     */
    PENDING("PENDING", "待审核"),

    /**
     * 通过
     */
    APPROVED("APPROVED", "通过"),

    /**
     * 拒绝
     */
    REJECTED("REJECTED", "拒绝"),
    /**
     * 已撤销
     */
    REVOKED("REVOKED", "已撤销"),

    /**
     * 需重新审核（商品修改了需要审核的字段）
     */
    REAUDIT("REAUDIT", "需重新审核");

    private final String code;
    private final String name;

    public static AuditStatus of(String code) {
        return Arrays.stream(AuditStatus.values())
                     .filter(status -> status.code.equals(code))
                     .findFirst()
                     .orElseThrow(() -> new IllegalArgumentException("未知的审核状态代码: " + code));
    }
}