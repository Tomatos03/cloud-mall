package com.onlineshop.framework.models.system.role.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色表单数据DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleFormData {
    private String name;
    private String description;
    private Boolean enabled;
}
