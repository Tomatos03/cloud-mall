package com.onlineshop.controller.common;

import com.onlineshop.framework.models.auth.dto.LoginDTO;
import com.onlineshop.framework.models.auth.dto.TokenDTO;
import com.onlineshop.framework.models.auth.service.IAuthService;
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
public class AuthController {
    @Autowired
    private IAuthService accountService;

    @PostMapping("/login")
    public TokenDTO login(@RequestBody LoginDTO loginDTO) {
        return accountService.login(loginDTO);
    }
}