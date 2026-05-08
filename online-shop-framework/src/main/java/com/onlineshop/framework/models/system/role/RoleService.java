package com.onlineshop.framework.models.system.role;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineshop.framework.models.system.resource.Resource;
import com.onlineshop.framework.models.system.resource.service.impl.ResourceService;
import com.onlineshop.framework.models.system.role.dto.RoleFormData;
import com.onlineshop.framework.models.system.role.dto.RolePageParamsDTO;
import com.onlineshop.framework.models.system.role.entity.Role;
import com.onlineshop.framework.models.system.relation.entity.RoleResource;
import com.onlineshop.framework.models.system.role.mapper.RoleMapper;
import com.onlineshop.framework.models.system.relation.mapper.RoleResourceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService extends ServiceImpl<RoleMapper, Role> implements IRoleService {
    private final RoleResourceMapper roleResourceMapper;
    private final ResourceService resourceService;

    @Override
    public List<Role> queryRolesByIds(List<Long> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery()
                .in(Role::getId, roleIds)
                .list();
    }

    @Override
    public IPage<Role> queryRoleList(RolePageParamsDTO query) {
        Page<Role> pageParam = new Page<>(query.getPage(), query.getPageSize());
        return lambdaQuery()
                .like(StringUtils.hasText(query.getName()), Role::getName, query.getName())
                .page(pageParam);
    }

    @Override
    public Role addRole(RoleFormData form) {
        Role role = Role.builder()
                        .name(form.getName())
                        .description(form.getDescription())
                        .enable(form.getEnabled())
                        .createTime(LocalDateTime.now())
                        .build();
        save(role);
        return role;
    }

    @Override
    public Role updateRole(Long id, RoleFormData form) {
        Role role = Role.builder()
                        .id(id)
                        .name(form.getName())
                        .description(form.getDescription())
                        .enable(form.getEnabled())
                        .build();
        updateById(role);
        return role;
    }

    @Override
    public void deleteRole(Long id) {
        removeById(id);
        // 删除关联的角色资源
        roleResourceMapper.delete(
                new LambdaQueryWrapper<RoleResource>()
                        .eq(RoleResource::getRoleId, id)
        );
    }

    @Override
    public List<Long> getRoleMenuIds(Long roleId) {
        List<RoleResource> roleResources = roleResourceMapper.selectList(
                new LambdaQueryWrapper<RoleResource>()
                        .eq(RoleResource::getRoleId, roleId)
        );
        return roleResources.stream()
                            .map(RoleResource::getResourceId)
                            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void assignMenus(Long roleId, List<Long> menuIds) {
        // 先删除旧的
        roleResourceMapper.delete(
                new LambdaQueryWrapper<RoleResource>()
                        .eq(RoleResource::getRoleId, roleId)
        );
        // 插入新的
        List<RoleResource> roleResources = menuIds.stream()
                                                   .map(menuId -> RoleResource.builder()
                                                                              .roleId(roleId)
                                                                              .resourceId(menuId)
                                                                              .build())
                                                   .toList();
        for (RoleResource rr : roleResources) {
            roleResourceMapper.insert(rr);
        }
    }

    @Override
    public List<Resource> getMenuResourcesByRoleIds(Collection<Long> roleIds) {
        List<RoleResource> roleResources = getRoleResourcesByRoleIds(roleIds);
        List<Long> resourceIds = convertDistinctResourceIds(roleResources);
        return resourceService.queryMenuResourcesByIds(resourceIds);
    }

    @Override
    public List<String> queryResourceCodesByRoleIds(Collection<Long> roleIds) {
        List<RoleResource> roleResources = getRoleResourcesByRoleIds(roleIds);
        List<Long> resourceIds = convertDistinctResourceIds(roleResources);
        List<Resource> resources = resourceService.getResourcesByIds(resourceIds);
        return filterConvertToResourceCodes(resources);
    }

    @Override
    public List<String> getResourceCodesByRoleNames(Collection<String> roleNames) {
        if (CollectionUtils.isEmpty(roleNames)) {
            return Collections.emptyList();
        }

        // 根据角色名称查询角色ID
        List<Role> roles = this.list(
                new LambdaQueryWrapper<Role>()
                        .in(Role::getName, roleNames)
        );

        if (CollectionUtils.isEmpty(roles)) {
            return Collections.emptyList();
        }

        // 提取角色ID并查询资源代码
        List<Long> roleIds = roles.stream()
                                  .map(Role::getId)
                                  .collect(Collectors.toList());

        return queryResourceCodesByRoleIds(roleIds);
    }

    private static List<String> filterConvertToResourceCodes(List<Resource> resources) {
        if (CollectionUtils.isEmpty(resources)) {
            return Collections.emptyList();
        }

        return resources.stream()
                        .filter(r ->
                                        r.getCode() != null && !r.getCode()
                                                                 .isBlank())
                        .map(Resource::getCode)
                        .collect(Collectors.toList());
    }

    private List<RoleResource> getRoleResourcesByRoleIds(Collection<Long> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return Collections.emptyList();
        }
        return roleResourceMapper.selectList(
                new LambdaQueryWrapper<RoleResource>()
                        .in(RoleResource::getRoleId, roleIds)
        );
    }

    private static List<Long> convertDistinctResourceIds(List<RoleResource> roleResources) {
        if (CollectionUtils.isEmpty(roleResources)) {
            return Collections.emptyList();
        }
        return roleResources.stream()
                            .map(RoleResource::getResourceId)
                            .distinct()
                            .collect(Collectors.toList());
    }
}
