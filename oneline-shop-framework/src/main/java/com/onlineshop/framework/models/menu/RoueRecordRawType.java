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
    VIEW("view");

    private final String code;
}
