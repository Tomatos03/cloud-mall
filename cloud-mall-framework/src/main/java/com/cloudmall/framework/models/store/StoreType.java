package com.cloudmall.framework.models.store;

import com.cloudmall.framework.common.enums.BizErrorCode;
import com.cloudmall.framework.exception.BizException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 店铺类型枚举
 *
 * @author : Tomatos
 * @date : 2026/2/16
 */
@Getter
@AllArgsConstructor
public enum StoreType {
    /**
     * 个人店铺
     */
    PERSONAL("personal", "个人店铺"),

    /**
     * 个体店铺
     */
    INDIVIDUAL("individual", "个体店铺"),

    /**
     * 企业店铺
     */
    ENTERPRISE("enterprise", "企业店铺");

    private final String code;
    private final String description;

    /**
     * 根据 code 获取店铺类型
     *
     * @param code 店铺类型代码
     * @return 店铺类型枚举
     */
    public static StoreType of(String code) {
        return Arrays.stream(values())
                     .filter(type -> type.code.equals(code))
                     .findFirst()
                     .orElseThrow(() -> new BizException(BizErrorCode.INVALID_PARAM));
    }

    /**
     * 是否个人店铺
     */
    public boolean isPersonal() {
        return this == PERSONAL;
    }

    /**
     * 是否个体店铺
     */
    public boolean isIndividual() {
        return this == INDIVIDUAL;
    }

    /**
     * 是否企业店铺
     */
    public boolean isEnterprise() {
        return this == ENTERPRISE;
    }
}
