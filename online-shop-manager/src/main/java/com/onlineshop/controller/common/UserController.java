package com.onlineshop.controller.common;

import com.onlineshop.framework.models.user.IUserService;
import com.onlineshop.framework.models.user.dto.ManagerUserInfoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/1/2
 */
@RestController
@RequestMapping("/manager/user")
public class UserController {
    @Autowired
    private IUserService userService;

    @GetMapping("/info")
    public ManagerUserInfoDTO getUserInfo() {
        return userService.getManagerUserInfo();
    }
}
