package com.onlineshop.framework.models.system.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineshop.framework.common.entity.PageParamsDTO;
import com.onlineshop.framework.models.system.role.RoleService;
import com.onlineshop.framework.models.system.role.entity.Role;
import com.onlineshop.framework.models.system.user.dto.UserUpdateDTO;
import com.onlineshop.framework.models.system.user.entity.User;
import com.onlineshop.framework.models.system.user.entity.UserRoles;
import com.onlineshop.framework.models.system.user.mapper.UserMapper;
import com.onlineshop.framework.models.system.user.mapper.UserRolesMapper;
import com.onlineshop.framework.models.system.user.vo.UserInfoVO;
import com.onlineshop.framework.utils.AuthUserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 *
 * @author Tomatos
 * @date 2025/12/20
 */
@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserMapper, User> implements IUserService {
    private final UserRolesMapper userRolesMapper;
    private final RoleService roleService;

    @Override
    public IPage<UserListItemVO> getUsersPage(PageParamsDTO pageParams) {
        Page<User> page = new Page<>(pageParams.getPage(), pageParams.getPageSize());
        return this.page(page)
                   .convert(this::convertToUserListItemVO);
    }

    private UserListItemVO convertToUserListItemVO(User user) {
        List<Role> roles = queryRolesByUserId(user.getId());
        return new UserListItemVO(user.getId(), user.getUsername(), convertRoleIdList(roles));
    }

    private List<UserRoles> queryUserRoles(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        return userRolesMapper.selectList(
                new LambdaQueryWrapper<UserRoles>()
                        .eq(UserRoles::getUserId, userId)
        );
    }

    private static List<Long> convertToRoleIds(List<UserRoles> userRoles) {
        if (CollectionUtils.isEmpty(userRoles)) {
            return Collections.emptyList();
        }

        return userRoles.stream()
                        .map(UserRoles::getRoleId)
                        .collect(Collectors.toList());
    }

    @Override
    public UserInfoVO getUserInfo() {
        User user = getById(AuthUserUtils.getUserId());

        return UserInfoVO.builder()
                         .nickname(user.getNickname())
                         .avatarUrl(user.getAvatarUrl())
                         .phone(user.getPhone())
                         .email(user.getEmail())
                         .bio(user.getBio())
                         .uid(
                                 user.getId()
                                     .toString()
                         )
                         .build();
    }

    @Override
    public boolean updateUserInfo(UserInfoVO userInfoVO) {
        return updateById(
                User.builder()
                    .id(AuthUserUtils.getUserId())
                    .nickname(userInfoVO.getNickname())
                    .avatarUrl(userInfoVO.getAvatarUrl())
                    .phone(userInfoVO.getPhone())
                    .email(userInfoVO.getEmail())
                    .bio(userInfoVO.getBio())
                    .build()
        );
    }

    @Override
    public User queryUserByUsername(String username) {
        return lambdaQuery()
                .eq(User::getUsername, username)
                .one();
    }

    @Override
    public List<Role> queryRolesByUserId(Long userId) {
        List<UserRoles> userRoles = queryUserRoles(userId);
        List<Long> roleIds = convertToRoleIds(userRoles);
        return roleService.queryRolesByIds(roleIds);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void assignRolesToUser(Long userId, List<Long> roleIds) {
        removeExistRolesForUser(userId);
        batchInsertUserRoles(userId, roleIds);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateUser(UserUpdateDTO updateDTO) {
        User user = User.builder()
                .id(updateDTO.getId())
                .username(updateDTO.getUsername())
                .build();
        
        if (StringUtils.hasText(updateDTO.getPassword())) {
            user.setPassword(updateDTO.getPassword());
        }

        updateById(user);

        if (!CollectionUtils.isEmpty(updateDTO.getRoleIds())) {
            assignRolesToUser(updateDTO.getId(), updateDTO.getRoleIds());
        }
    }

    private void removeExistRolesForUser(Long userId) {
        userRolesMapper.delete(
                new LambdaQueryWrapper<UserRoles>()
                        .eq(UserRoles::getUserId, userId)
        );
    }

    private void batchInsertUserRoles(Long userId, List<Long> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return;
        }
        List<UserRoles> userRoles = createUserRolesList(userId, roleIds);
        for (UserRoles userRole : userRoles) {
            userRolesMapper.insert(userRole);
        }
    }

    private static List<UserRoles> createUserRolesList(Long userId, List<Long> roleIds) {
        return roleIds.stream()
                      .map(roleId -> UserRoles.builder()
                                              .userId(userId)
                                              .roleId(roleId)
                                              .build())
                      .toList();
    }

    private List<Long> convertRoleIdList(List<Role> roles) {
        if (CollectionUtils.isEmpty(roles)) {
            return Collections.emptyList();
        }

        return roles.stream()
                    .map(Role::getId)
                    .collect(Collectors.toList());
    }
}