package com.cloudmall.framework.models.system.role.dto;

import com.cloudmall.framework.common.entity.PageParamsDTO;
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
