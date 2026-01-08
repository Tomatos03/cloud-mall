package com.onlineshop.framework.models.menu;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/1/3
 */
@Getter
@AllArgsConstructor
public enum RoueRecordRawType {
    LAYOUT("layout"),
    VIEW("view"), // 叶子节点视图
    PARENT_VIEW("parentView"); // 非叶子节点视图

    private final String code;
}
