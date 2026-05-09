package com.cloudmall.framework.models.auth.service.impl;

import com.cloudmall.framework.common.enums.BizErrorCode;
import com.cloudmall.framework.models.auth.bo.TokenPayload;
import com.cloudmall.framework.models.auth.dto.LoginDTO;
import com.cloudmall.framework.models.auth.dto.RegisterDTO;
import com.cloudmall.framework.models.auth.dto.TokenDTO;
import com.cloudmall.framework.models.auth.enums.AccountType;
import com.cloudmall.framework.models.auth.service.IAuthService;
import com.cloudmall.framework.models.auth.service.ITokenService;
import com.cloudmall.framework.models.system.user.IUserService;
import com.cloudmall.framework.models.system.user.entity.User;
import com.cloudmall.framework.security.AuthUser;
import com.cloudmall.framework.utils.AssertUtils;
import com.cloudmall.framework.utils.AuthUserUtils;
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

    public TokenDTO login(LoginDTO loginDTO, AccountType accountType) {
        UsernamePasswordAuthenticationToken authenticationRequest =
                new UsernamePasswordAuthenticationToken(
                        loginDTO.getUsername(), loginDTO.getPassword()
                );
        Authentication authenticate = authenticationManager.authenticate(authenticationRequest);
        AuthUser authUser = (AuthUser) authenticate.getPrincipal();
        
        validateAccountType(authUser, accountType);
        
        return createToken(accountType, authUser);
    }

    private TokenDTO createToken(AccountType type, AuthUser authUser) {
        return TokenDTO.builder()
                       .token(tokenService.generate(buildTokenPayload(authUser, type)))
                       .build();
    }

    private void validateAccountType(AuthUser user, AccountType accountType) {
        AssertUtils.contains(user.getAvailableAccountTypes(), accountType.getCode(), BizErrorCode.USERNAME_OR_PASSWORD_ERROR);
    }

    @Override
    public boolean register(RegisterDTO registerDTO) {
        String username = registerDTO.getUsername();
        String password = registerDTO.getPassword();
        User user = userService.queryUserByUsername(username);
        AssertUtils.isNull(user, BizErrorCode.USER_ALREADY_EXISTS);

        userService.save(
                User.builder()
                    .username(username)
                    .password(passwordEncoder.encode(password))
                    .nickname("用户" + username)
                    .types(AccountType.NORMAL.getCode())
                    .build()
        );
        return true;
    }

    @Override
    public boolean changePassword(String newPassword) {
        User user = userService.getById(AuthUserUtils.getUserId());
        AssertUtils.notNull(user, BizErrorCode.USER_NOT_EXISTS);

        user.setPassword(passwordEncoder.encode(newPassword));
        return userService.updateById(user);
    }

    private TokenPayload buildTokenPayload(@NonNull AuthUser user, AccountType accountType) {
        return TokenPayload.builder()
                           .userId(user.getUserId())
                           .username(user.getUsername())
                           .roles(user.getRoleCodes())
                           .storeId(user.getStoreId())
                           .accountType(accountType.getCode())
                           .build();
    }
}
