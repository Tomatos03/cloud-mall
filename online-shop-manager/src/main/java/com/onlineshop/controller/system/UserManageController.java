package com.onlineshop.controller.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlineshop.framework.common.entity.PageParamsDTO;
import com.onlineshop.framework.models.system.application.ISystemAppService;
import com.onlineshop.framework.models.system.user.IUserService;
import com.onlineshop.framework.models.system.user.UserListItemVO;
import com.onlineshop.framework.models.system.user.entity.User;
import com.onlineshop.framework.models.system.user.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 用户管理控制器
 *
 * @author : Tomatos
 * @date : 2025/12/20
 */
@RestController
@RequestMapping("/manage/user")
@PreAuthorize("hasAuthority('user:view')")
@RequiredArgsConstructor
public class UserManageController {
    private final IUserService userService;
    private final ISystemAppService systemAppService;

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息DTO
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/info")
    public UserInfoVO getUserInfo() {
        return systemAppService.getUserInfo();
    }

    /**
     * 创建用户（管理员权限）
     *
     * @param user 用户信息
     * @return 是否成功
     */
    @PostMapping
    @PreAuthorize("hasAuthority('user:add')")
    public boolean createUser(@RequestBody User user) {
        return userService.save(user);
    }

    /**
     * 根据ID查询用户（管理员权限）
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getById(id);
    }

    /**
     * 获取所有用户列表（管理员权限）
     *
     * @return 用户列表
     */
    @GetMapping
    public List<User> getAllUsers() {
        return userService.list();
    }

    /**
     * 修改用户信息（管理员权限）
     *
     * @param user 用户信息
     * @return 是否成功
     */
    @PutMapping
    @PreAuthorize("hasAuthority('user:edit')")
    public boolean updateUser(@RequestBody User user) {
        return userService.updateById(user);
    }

    /**
     * 删除用户（管理员权限）
     *
     * @param id 用户ID
     * @return 是否成功
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user:delete')")
    public boolean deleteUser(@PathVariable Long id) {
        return userService.removeById(id);
    }

    /**
     * 分页查询用户列表（管理员权限）
     *
     * @param pageParams 分页参数
     * @return 用户分页结果
     */
    @GetMapping("/page")
    public IPage<UserListItemVO> getUsersPage(PageParamsDTO pageParams) {
        return userService.getUsersPage(pageParams);
    }

    /**
     * 为用户分配角色（管理员权限）
     *
     * @param userId 用户ID
     * @param request 分配角色请求
     * @return 是否成功
     */
    @PutMapping("/{userId}/roles")
    @PreAuthorize("hasAuthority('user:edit')")
    public boolean assignRolesToUser(@PathVariable Long userId, @RequestBody List<Long> roleIds) {
        userService.assignRolesToUser(userId, roleIds);
        return true;
    }
}