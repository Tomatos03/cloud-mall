package com.onlineshop.framework.models.auth.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String role;
    private String username;
    private Long storeId;
}
