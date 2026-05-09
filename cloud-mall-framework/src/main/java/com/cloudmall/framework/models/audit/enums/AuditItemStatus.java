package com.cloudmall.framework.models.audit.enums;

import com.cloudmall.framework.common.enums.BizErrorCode;
import com.cloudmall.framework.exception.BizException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 审核项目状态枚举
 * 
 * 表示audit_item表中的status字段值
 */
@AllArgsConstructor
@Getter
public enum AuditItemStatus {
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
    REJECTED("REJECTED", "已拒绝");

    private final String code;
    private final String name;

    public static AuditItemStatus of(String code) {
        return Arrays.stream(AuditItemStatus.values())
                     .filter(status -> status.code.equals(code))
                     .findFirst()
                     .orElseThrow(() -> new BizException(BizErrorCode.INVALID_AUDIT_STATUS));
    }
}
