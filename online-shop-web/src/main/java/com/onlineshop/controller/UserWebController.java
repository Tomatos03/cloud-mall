package com.onlineshop.controller;

import com.onlineshop.framework.models.auth.dto.LoginDTO;
import com.onlineshop.framework.models.auth.dto.RegisterDTO;
import com.onlineshop.framework.models.auth.dto.TokenDTO;
import com.onlineshop.framework.models.auth.enums.AccountType;
import com.onlineshop.framework.models.auth.service.IAuthService;
import com.onlineshop.framework.models.system.user.IUserService;
import com.onlineshop.framework.models.system.user.vo.UserInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


/**
 * 用户控制器
 *
 * @author Tomatos
 * @date 2025/12/17
 */
@RestController
@RequestMapping("/web/user")
public class UserWebController {
    @Autowired
    private IAuthService authService;
    @Autowired
    private IUserService userService;

    /**
     * 用户登录
     *
     * @return 用户信息
     */
    @PostMapping("/login")
    public TokenDTO login(@RequestBody LoginDTO loginDTO) {
        return authService.login(loginDTO, AccountType.NORMAL);
    }

    /**
     * 用户注册
     *
     * @return 是否成功
     */
    @PostMapping("/register")
    public boolean register(@RequestBody RegisterDTO registerDTO) {
        return authService.register(registerDTO);
    }

    /**
     * 修改密码
     *
     * @return 是否成功
     */
    @PostMapping("/changePassword")
    public boolean changePassword(@RequestBody String newPassword) {
        return authService.changePassword(newPassword);
    }

    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/info")
    public UserInfoVO getUserInfo() {
        return userService.getUserInfo();
    }

    @PutMapping("/info")
    public boolean updateUserInfo(@RequestBody UserInfoVO userInfoVO) {
        return userService.updateUserInfo(userInfoVO);
    }
}