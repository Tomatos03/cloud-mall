package com.cloudmall.controller.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cloudmall.framework.models.system.role.IRoleService;
import com.cloudmall.framework.models.system.role.dto.RoleFormData;
import com.cloudmall.framework.models.system.role.dto.RolePageParamsDTO;
import com.cloudmall.framework.models.system.role.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 */
@RestController
@RequestMapping("/manager/system/role")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('role:view')")
public class RoleManageController {

    private final IRoleService roleService;

    /**
     * 分页查询角色列表
     */
    @GetMapping("/page")
    public IPage<Role> getRoleList(RolePageParamsDTO query) {
        return roleService.queryRoleList(query);
    }

    /**
     * 新增角色
     */
    @PostMapping
    @PreAuthorize("hasAuthority('role:add')")
    public Role addRole(@RequestBody RoleFormData form) {
        return roleService.addRole(form);
    }

    /**
     * 更新角色
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('role:edit')")
    public Role updateRole(@PathVariable Long id, @RequestBody RoleFormData form) {
        return roleService.updateRole(id, form);
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('role:delete')")
    public void deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
    }

    /**
     * 获取角色菜单权限
     */
    @GetMapping("/{roleId}/menus")
    public List<Long> getRoleMenus(@PathVariable Long roleId) {
        return roleService.getRoleMenuIds(roleId);
    }

    /**
     * 分配菜单权限
     */
    @PostMapping("/{roleId}/assign-menus")
    @PreAuthorize("hasAuthority('role:edit')")
    public void assignMenus(@PathVariable Long roleId, @RequestBody List<Long> menuIds) {
        roleService.assignMenus(roleId, menuIds);
    }
}
