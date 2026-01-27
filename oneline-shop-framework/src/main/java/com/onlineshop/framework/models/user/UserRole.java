package com.onlineshop.framework.models.user;

import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 用户角色枚举
 *
 * @author Tomatos
 * @date 2025/12/17
 */
@Getter
@AllArgsConstructor
public enum UserRole {
    /**
     * 普通用户
     */
    NORMAL("NORMAL"),

    /**
     * 管理员
     */
    ADMIN("ADMIN"),

    /**
     * 商家
     */
    MERCHANT("MERCHANT");

    /**
     * 角色代码
     */
    private final String code;

    public static UserRole of(String code) {
        return Arrays.stream(values())
                     .filter(accountRole -> accountRole.code.equals(code))
                     .findFirst()
                     .orElseThrow(() -> new BusinessException(BizErrorCode.UNKNOWN_ROLE));
    }
}