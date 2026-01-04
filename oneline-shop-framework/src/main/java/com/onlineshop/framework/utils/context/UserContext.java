package com.onlineshop.framework.utils.context;

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
@AllArgsConstructor
@NoArgsConstructor
public class UserContext {
    private Long id;
    private String username;
    private String roleCode;
    private Long storeId;
}
