package com.cloudmall.framework.models.auth.enums;

import com.cloudmall.framework.common.enums.BizErrorCode;
import com.cloudmall.framework.exception.BizException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 客户端类型枚举
 *
 * @author : Tomatos
 * @date : 2026/03/04
 */
@Getter
@AllArgsConstructor
public enum AccountType {

    /**
     * 普通用户（消费者端）
     */
    NORMAL("NORMAL", "普通用户"),

    /**
     * 商户端（商家管理端）
     */
    MERCHANT("MERCHANT", "商户端"),

    /**
     * 管理端（平台运营管理）
     */
    ADMIN("ADMIN", "管理员");

    private final String code;
    private final String description;

    public static AccountType of(String code) {
        return Arrays.stream(values())
                .filter(type -> type.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new BizException(BizErrorCode.INVALID_PARAM));
    }

    public boolean isNormal() {
        return this == NORMAL;
    }

    public boolean isMerchant() {
        return this == MERCHANT;
    }

    public boolean isAdmin() {
        return this == ADMIN;
    }
}
