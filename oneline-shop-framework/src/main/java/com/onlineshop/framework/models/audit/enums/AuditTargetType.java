package com.onlineshop.framework.models.audit.enums;

/**
 * 审核对象类型枚举
 */
public enum AuditTargetType {
    /**
     * 商品(SPU)
     */
    GOODS("GOODS", "商品"),

    /**
     * 商品规格(SKU)
     */
    SKU("SKU", "商品规格"),

    /**
     * 其他
     */
    OTHER("OTHER", "其他");

    private final String code;
    private final String name;

    AuditTargetType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static AuditTargetType fromCode(String code) {
        for (AuditTargetType type : AuditTargetType.values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}