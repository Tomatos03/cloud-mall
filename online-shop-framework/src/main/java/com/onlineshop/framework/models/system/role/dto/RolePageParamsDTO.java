package com.onlineshop.framework.models.system.role.dto;

import com.onlineshop.framework.common.entity.PageParamsDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色分页查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RolePageParamsDTO extends PageParamsDTO {
    private String name;
}
