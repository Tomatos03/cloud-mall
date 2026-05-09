package com.cloudmall.controller;

import com.cloudmall.framework.models.auth.dto.LoginDTO;
import com.cloudmall.framework.models.auth.dto.TokenDTO;
import com.cloudmall.framework.models.auth.enums.AccountType;
import com.cloudmall.framework.models.auth.service.IAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器
 *
 * @author : Tomatos
 * @date : 2025/12/20
 */
@RequestMapping("/manager/auth")
@RestController
public class AuthManageController {
    @Autowired
    private IAuthService authService;

    @PostMapping("/login")
    public TokenDTO login(@RequestBody LoginDTO loginDTO) {
        return authService.login(loginDTO, AccountType.ADMIN);
    }
}