package com.onlineshop.framework.models.auth.service.impl;

import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.exception.BizException;
import com.onlineshop.framework.models.auth.bo.TokenPayload;
import com.onlineshop.framework.models.auth.dto.LoginDTO;
import com.onlineshop.framework.models.auth.dto.RegisterDTO;
import com.onlineshop.framework.models.auth.dto.TokenDTO;
import com.onlineshop.framework.models.auth.service.IAuthService;
import com.onlineshop.framework.models.auth.service.ITokenService;
import com.onlineshop.framework.models.store.IStoreService;
import com.onlineshop.framework.models.system.user.IUserService;
import com.onlineshop.framework.models.system.user.entity.User;
import com.onlineshop.framework.security.AuthUser;
import com.onlineshop.framework.utils.AuthUserUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 *
 * @author Tomatos
 * @date 2025/12/17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {
    private final IUserService userService;
    private final ITokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final IStoreService storeService;

    public TokenDTO login(LoginDTO loginDTO) {
        UsernamePasswordAuthenticationToken authenticationRequest =
                new UsernamePasswordAuthenticationToken(
                        loginDTO.getUsername(), loginDTO.getPassword()
                );
        Authentication authenticate = authenticationManager.authenticate(authenticationRequest);
        AuthUser authUser = (AuthUser) authenticate.getPrincipal();
        return TokenDTO.builder()
                       .token(tokenService.generate(buildTokenPayload(authUser)))
                       .build();
    }

    @Override
    public boolean register(RegisterDTO registerDTO) {
        String username = registerDTO.getUsername();
        String password = registerDTO.getPassword();
        User user = userService.queryUserByUsername(username);
        validateUserNotExist(user);

        userService.save(
                User.builder()
                    .username(username)
                    .password(passwordEncoder.encode(password))
                    .nickname("用户" + username)
                    .build()
        );
        return true;
    }

    private static void validateUserNotExist(User user) {
        if (user != null) {
            throw new BizException(BizErrorCode.USER_ALREADY_EXISTS);
        }
    }

    @Override
    public boolean changePassword(String newPassword) {
        User user = userService.getById(AuthUserUtils.getUserId());
        validateUserExist(user);

        user.setPassword(passwordEncoder.encode(newPassword));
        return userService.updateById(user);
    }

    private static void validateUserExist(User user) {
        if (user == null) {
            throw new BizException(BizErrorCode.USER_NOT_EXISTS);
        }
    }

    private TokenPayload buildTokenPayload(@NonNull AuthUser user) {
        return TokenPayload.builder()
                           .userId(user.getUserId())
                           .username(user.getUsername())
                           .roles(user.getRoleCodes())
                           .storeId(user.getStoreId())
                           .build();
    }
}