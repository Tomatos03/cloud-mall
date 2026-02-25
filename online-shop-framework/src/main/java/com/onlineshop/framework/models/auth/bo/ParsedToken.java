package com.onlineshop.framework.models.auth.bo;

import lombok.*;

import java.util.List;

/**
 *
 *
 * @author : Tomatos
 * @date : 2025/12/31
 */
@Data
@AllArgsConstructor
public class ParsedToken {
    private Long userId;
    private String username;
    private List<String> roles;
    private Long storeId;
    private String accountType;
}