package com.cloudmall.framework.models.auth.service;

import com.cloudmall.framework.models.auth.dto.LoginDTO;
import com.cloudmall.framework.models.auth.dto.RegisterDTO;
import com.cloudmall.framework.models.auth.dto.TokenDTO;
import com.cloudmall.framework.models.auth.enums.AccountType;

/**
 * 用户服务接口
 *
 * @author Tomatos
 * @date 2025/12/17
 */
public interface IAuthService {
    TokenDTO login(LoginDTO loginDTO, AccountType accountType);

    boolean register(RegisterDTO registerDTO);

    /**
     * 修改密码
     *
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean changePassword(String newPassword);
}