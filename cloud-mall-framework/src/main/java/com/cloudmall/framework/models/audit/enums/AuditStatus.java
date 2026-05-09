package com.cloudmall.framework.models.audit.enums;

import com.cloudmall.framework.common.enums.BizErrorCode;
import com.cloudmall.framework.exception.BizException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 审核批次状态枚举
 * <p>
 * 包含：
 * PENDING("PENDING", "待审核") - 初始状态，批次刚创建
 * APPROVED("APPROVED", "已通过") - 所有项都通过
 * REJECTED("REJECTED", "已拒绝") - 所有项都拒绝
 * PARTIAL("PARTIAL", "部分通过") - 部分项通过，部分项拒绝
 * REVOKED("REVOKED", "已撤销") - 申请人已撤销该批次
 */
@AllArgsConstructor
@Getter
public enum AuditStatus {
    /**
     * 待审核
     */
    PENDING("PENDING", "待审核"),

    /**
     * 已通过
     */
    APPROVED("APPROVED", "已通过"),

    /**
     * 已拒绝
     */
    REJECTED("REJECTED", "已拒绝"),

    /**
     * 部分通过
     */
    PARTIAL("PARTIAL", "部分通过"),

    /**
     * 已撤销
     */
    REVOKED("REVOKED", "已撤销");

    private final String code;
    private final String name;

    public static AuditStatus of(String code) {
        return Arrays.stream(AuditStatus.values())
                     .filter(status -> status.code.equals(code))
                     .findFirst()
                     .orElseThrow(() -> new BizException(BizErrorCode.INVALID_AUDIT_STATUS));
    }
}
