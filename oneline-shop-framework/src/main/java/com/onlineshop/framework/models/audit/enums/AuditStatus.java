package com.onlineshop.framework.models.audit.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 商品审核状态枚举
 * <p>
 * 包含：
 * PENDING(0, "待审核")，APPROVED(1, "通过")，REJECTED(2, "拒绝")，CANCELLED(3, "已撤销")
 */
@AllArgsConstructor
@Getter
public enum AuditStatus {
    /**
     * 待审核 (0)
     */
    PENDING(0, "待审核"),

    /**
     * 通过 (1)
     */
    APPROVED(1, "通过"),

    /**
     * 拒绝 (2)
     */
    REJECTED(2, "拒绝"),
    /**
     * 已撤销 (3)
     */
    REVOKED(3, "已撤销");

    private final int code;
    private final String name;

    public static AuditStatus of(int code) {
        return Arrays.stream(AuditStatus.values())
                     .filter(status -> status.code == code)
                     .findFirst()
                     .orElseThrow(() -> new IllegalArgumentException("未知的审核状态代码: " + code));
    }
}