package com.cloudmall.framework.models.system.role;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudmall.framework.models.system.resource.Resource;
import com.cloudmall.framework.models.system.role.dto.RoleFormData;
import com.cloudmall.framework.models.system.role.dto.RolePageParamsDTO;
import com.cloudmall.framework.models.system.role.entity.Role;

import java.util.Collection;
import java.util.List;

/**
 * 角色服务接口
 *
 * @author Tomatos
 * @date 2026/2/3
 */
public interface IRoleService extends IService<Role> {
    List<Role> queryRolesByIds(List<Long> roleIds);

    /**
     * 分页查询角色列表
     */
    IPage<Role> queryRoleList(RolePageParamsDTO query);

    /**
     * 新增角色
     */
    Role addRole(RoleFormData form);

    /**
     * 更新角色
     */
    Role updateRole(Long id, RoleFormData form);

    /**
     * 删除角色
     */
    void deleteRole(Long id);

    /**
     * 获取角色菜单权限
     */
    List<Long> getRoleMenuIds(Long roleId);

    /**
     * 分配菜单权限
     */
    void assignMenus(Long roleId, List<Long> menuIds);

    /**
     * 获取指定角色拥有的菜单资源
     *
     * @param roleIds 角色ID集合
     * @return 资源列表
     */
    List<Resource> getMenuResourcesByRoleIds(Collection<Long> roleIds);

    /**
     * 获取指定角色列表的资源代码权限
     *
     * @param roleIds 角色ID列表
     * @return 资源代码列表，格式为 businessName:action
     */
    List<String> queryResourceCodesByRoleIds(Collection<Long> roleIds);

    /**
     * 根据角色名称列表获取资源代码权限
     *
     * @param roleNames 角色名称列表（如 ["ROLE_ADMIN", "ROLE_USER"]）
     * @return 资源代码列表，格式为 businessName:action，已去重
     */
    List<String> getResourceCodesByRoleNames(Collection<String> roleNames);
}