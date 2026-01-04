package com.onlineshop.framework.models.auth.bo;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *
 *
 * @author : Tomatos
 * @date : 2025/12/31
 */
@Getter
@AllArgsConstructor
public class ParsedToken {
    private final Long userId;
    private final String username;
    private final String role;
    private final Long storeId;
}