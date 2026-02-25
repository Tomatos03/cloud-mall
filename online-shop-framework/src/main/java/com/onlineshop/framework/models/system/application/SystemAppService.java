package com.onlineshop.framework.models.system.application;

import cn.hutool.core.bean.BeanUtil;
import com.onlineshop.framework.models.system.resource.Resource;
import com.onlineshop.framework.models.system.resource.ResourceMeta;
import com.onlineshop.framework.models.system.resource.enums.ResourceType;
import com.onlineshop.framework.models.system.resource.service.IResourceService;
import com.onlineshop.framework.models.system.resource.vo.MenuNodeVO;
import com.onlineshop.framework.models.system.role.IRoleService;
import com.onlineshop.framework.models.system.role.entity.Role;
import com.onlineshop.framework.models.system.user.IUserService;
import com.onlineshop.framework.models.system.user.entity.User;
import com.onlineshop.framework.models.system.user.vo.UserInfoVO;
import com.onlineshop.framework.utils.AuthUserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统应用服务实现类
 * 提供系统级别的应用功能实现
 *
 * @author Tomatos
 * @date 2026/2/3
 */
@Service
@RequiredArgsConstructor
public class SystemAppService implements ISystemAppService {
    private static final Long ROOT_MENU_NODE_ID = 0L;
    private final IUserService userService;
    private final IRoleService roleService;
    private final IResourceService resourceService;

    @Override
    public UserInfoVO getUserInfo() {
        User user = userService.getById(AuthUserUtils.getUserId());
        return UserInfoVO.builder()
                         .uid("u" + user.getId())
                         .nickname(user.getUsername())
                         .username(user.getNickname())
                         .phone(user.getPhone())
                         .email(user.getEmail())
                         .avatarUrl(user.getAvatarUrl())
                         .resourceCodes(queryUserResourceCodes())
                         .build();
    }

    @Override
    public MenuNodeVO getUserMenuTree() {
        List<Role> roles = userService.queryRolesByUserId(AuthUserUtils.getUserId());
        List<Resource> menuResources = roleService.getMenuResourcesByRoleIds(convertRoleId(roles));
        Map<Long, List<Resource>> map = groupingMenuNodeByParentId(menuResources);
        MenuNodeVO menuRootNode = buildMenuTreeVO(createMenuRootNode(), map);
        setRedirectToFirstMenu(menuRootNode);
        return menuRootNode;
    }

    private void setRedirectToFirstMenu(MenuNodeVO root) {
        List<MenuNodeVO> children = root.getChildren();
        if (CollectionUtils.isEmpty(children)) {
            return;
        }
        ResourceMeta meta = root.getMeta();
        meta.setRedirect(children.get(0).getMeta().getPath());
    }

    private List<String> queryUserResourceCodes() {
        List<Role> userRoles = userService.queryRolesByUserId(AuthUserUtils.getUserId());
        return roleService.queryResourceCodesByRoleIds(convertRoleId(userRoles));
    }

    @Override
    public MenuNodeVO queryResourceTree() {
        List<Resource> menuResources = resourceService.queryResources();
        Map<Long, List<Resource>> map = groupingMenuNodeByParentId(menuResources);
        return buildMenuTreeVO(createMenuRootNode(), map);
    }

    private static List<Long> convertRoleId(List<Role> userRoles) {
        if (CollectionUtils.isEmpty(userRoles)) {
            return Collections.emptyList();
        }

        return userRoles.stream()
                        .map(Role::getId)
                        .collect(Collectors.toList());
    }

    private static MenuNodeVO buildMenuTreeVO(
            MenuNodeVO parentNode,
            Map<Long, List<Resource>> parentIdToResourcesMap
    ) {
        List<Resource> childResources = parentIdToResourcesMap.getOrDefault(
                parentNode.getId(),
                Collections.emptyList()
        );
        List<MenuNodeVO> menuNodes = convertAndSortMenuTreeList(childResources);
        parentNode.setChildren(menuNodes);

        for (MenuNodeVO childNode : menuNodes) {
            buildMenuTreeVO(childNode, parentIdToResourcesMap);
        }
        return parentNode;
    }

    private static List<MenuNodeVO> convertAndSortMenuTreeList(List<Resource> menuResources) {
        if (CollectionUtils.isEmpty(menuResources)) {
            return Collections.emptyList();
        }
        return menuResources.stream()
                            .map(resource -> BeanUtil.copyProperties(resource, MenuNodeVO.class))
                            .sorted(Comparator.comparingInt(x -> x.getSort() == null ? 0 : x.getSort()))
                            .toList();
    }

    private static MenuNodeVO createMenuRootNode() {
        ResourceMeta meta = new ResourceMeta();
        meta.setPath("/");
        meta.setComponent("home");
        meta.setName("home");

        return MenuNodeVO.builder()
                         .id(ROOT_MENU_NODE_ID)
                         .description("首页")
                         .meta(meta)
                         .type(ResourceType.LAYOUT.getCode())
                         .build();
    }

    private static Map<Long, List<Resource>> groupingMenuNodeByParentId(List<Resource> resources) {
        return resources.stream()
                        .collect(
                                Collectors.groupingBy(
                                        r ->
                                                r.getParentId() == null
                                                        ? ROOT_MENU_NODE_ID
                                                        : r.getParentId()
                                )
                        );
    }
}
