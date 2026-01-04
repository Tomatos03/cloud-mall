package com.onlineshop.framework.models.auth.service;

import com.onlineshop.framework.models.auth.dto.LoginDTO;
import com.onlineshop.framework.models.auth.dto.RegisterDTO;
import com.onlineshop.framework.models.auth.dto.TokenDTO;

/**
 * 用户服务接口
 *
 * @author Tomatos
 * @date 2025/12/17
 */
public interface IAuthService {
    TokenDTO login(LoginDTO loginDTO);

    boolean register(RegisterDTO registerDTO);

    /**
     * 修改密码
     *
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean changePassword(String newPassword);
}