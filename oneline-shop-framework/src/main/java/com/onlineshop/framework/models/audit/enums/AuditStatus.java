package com.onlineshop.framework.models.audit.enums;

/**
 * 审核状态枚举
 */
public enum AuditStatus {
    /**
     * 未提交 (0)
     */
    NOT_SUBMITTED((byte) 0, "未提交"),

    /**
     * 待审核 (1)
     */
    PENDING((byte) 1, "待审核"),

    /**
     * 通过 (2)
     */
    APPROVED((byte) 2, "通过"),

    /**
     * 拒绝 (3)
     */
    REJECTED((byte) 3, "拒绝");

    private final byte code;
    private final String name;

    AuditStatus(byte code, String name) {
        this.code = code;
        this.name = name;
    }

    public byte getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static AuditStatus fromCode(byte code) {
        for (AuditStatus status : AuditStatus.values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}