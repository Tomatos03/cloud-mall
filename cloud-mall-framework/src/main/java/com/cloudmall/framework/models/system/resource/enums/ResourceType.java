package com.cloudmall.framework.models.system.resource.enums;

import com.cloudmall.framework.common.enums.BizErrorCode;
import com.cloudmall.framework.exception.BizException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 资源类型枚举
 * 用于定义系统中所有的资源类型
 *
 * @author : Tomatos
 * @date : 2025/01/24
 */
@Getter
@AllArgsConstructor
public enum ResourceType {
    /**
     * 菜单类型资源
     */
    MENU("menu", "菜单"),

    CATALOG("catalog", "目录"),

    /**
     * 布局类型资源
     */
    LAYOUT("layout", "布局"),
    BUTTON("button", "按钮"),
    ;

    private final String code;
    private final String description;

    /**
     * 根据 code 获取资源类型
     *
     * @param code 资源类型代码
     * @return 资源类型枚举
     */
    public static ResourceType of(String code) {
        return Arrays.stream(values())
                     .filter(type -> type.code.equals(code))
                     .findFirst()
                     .orElseThrow(() -> new BizException(BizErrorCode.INVALID_RESOURCE_TYPE));
    }
}