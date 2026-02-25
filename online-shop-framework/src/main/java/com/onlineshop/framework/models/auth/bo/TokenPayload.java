package com.onlineshop.framework.models.auth.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 *
 *
 * @author : Tomatos
 * @date : 2025/12/31
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenPayload {
    private Long userId;
    private List<String> roles;
    private String username;
    private Long storeId;
    private String accountType;
}
