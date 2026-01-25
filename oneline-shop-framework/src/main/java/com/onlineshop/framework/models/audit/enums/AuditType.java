package com.onlineshop.framework.models.audit.enums;

/**
 * 审核对象类型枚举
 */
public enum AuditType {
    /**
     * 商品(SPU)
     */
    GOODS("GOODS", "商品"),

    /**
     * 商品规格(SKU)
     */
    SKU("SKU", "商品规格");

    private final String code;
    private final String name;

    AuditType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static AuditType fromCode(String code) {
        for (AuditType type : AuditType.values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}