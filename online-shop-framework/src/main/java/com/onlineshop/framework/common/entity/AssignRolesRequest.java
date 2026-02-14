package com.onlineshop.framework.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分配角色请求DTO
 *
 * @author Tomatos
 * @date 2026/02/11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignRolesRequest {
    private List<Long> roleIds;
}
