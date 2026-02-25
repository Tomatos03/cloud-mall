package com.onlineshop.framework.models.system.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.onlineshop.framework.common.entity.PageParamsDTO;
import com.onlineshop.framework.models.system.role.entity.Role;
import com.onlineshop.framework.models.system.user.dto.UserUpdateDTO;
import com.onlineshop.framework.models.system.user.entity.User;
import com.onlineshop.framework.models.system.user.vo.UserInfoVO;

import java.util.List;

/**
 * 用户服务接口
 *
 * @author Tomatos
 * @date 2025/12/20
 */
public interface IUserService extends IService<User> {
    IPage<UserListItemVO> getUsersPage(PageParamsDTO pageParams);

    UserInfoVO getUserInfo();

    boolean updateUserInfo(UserInfoVO userInfoVO);

    User queryUserByUsername(String username);

    /**
     * 获取用户拥有的角色（可能有多个）
     *
     * @param userId 用户ID
     * @return 用户拥有的角色列表
     */
    List<Role> queryRolesByUserId(Long userId);

    /**
     * 为用户分配角色
     *
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     */
    void assignRolesToUser(Long userId, List<Long> roleIds);

    /**
     * 修改用户信息（包含角色）
     *
     * @param updateDTO 用户更新信息
     */
    void updateUser(UserUpdateDTO updateDTO);
}