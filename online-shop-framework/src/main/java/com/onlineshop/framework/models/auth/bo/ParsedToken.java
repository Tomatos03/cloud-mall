package com.onlineshop.framework.models.auth.bo;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

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
    private final List<String> roles;
    private final Long storeId;
}