package com.onlineshop.controller;

import com.onlineshop.framework.models.auth.dto.LoginDTO;
import com.onlineshop.framework.models.auth.dto.TokenDTO;
import com.onlineshop.framework.models.auth.enums.AccountType;
import com.onlineshop.framework.models.auth.service.IAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/2/11
 */

@RequestMapping("/auth")
@RestController
public class AuthMerchantController {
    @Autowired
    private IAuthService authService;

    @PostMapping("/login")
    public TokenDTO login(@RequestBody LoginDTO loginDTO) {
        return authService.login(loginDTO, AccountType.MERCHANT);
    }
}