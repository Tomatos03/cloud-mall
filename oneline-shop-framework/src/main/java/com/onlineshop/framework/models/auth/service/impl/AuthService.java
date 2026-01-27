package com.onlineshop.framework.models.auth.service.impl;

import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.exception.BusinessException;
import com.onlineshop.framework.models.auth.bo.TokenPayload;
import com.onlineshop.framework.models.auth.dto.LoginDTO;
import com.onlineshop.framework.models.auth.dto.RegisterDTO;
import com.onlineshop.framework.models.auth.dto.TokenDTO;
import com.onlineshop.framework.models.auth.service.IAuthService;
import com.onlineshop.framework.models.auth.service.ITokenProvider;
import com.onlineshop.framework.models.store.IStoreService;
import com.onlineshop.framework.models.store.Store;
import com.onlineshop.framework.models.user.IUserService;
import com.onlineshop.framework.models.user.User;
import com.onlineshop.framework.utils.context.UserContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 *
 * @author Tomatos
 * @date 2025/12/17
 */
@Slf4j
@Service
public class AuthService implements IAuthService {
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private IStoreService storeService;
    @Autowired
    private IUserService userService;
    @Autowired
    private ITokenProvider tokenProvider;

    @Override
    public TokenDTO login(LoginDTO loginDTO) {
        User user = queryUserByUsername(loginDTO.getUsername());
        validateUser(user);
        validatePassword(loginDTO.getPassword(), user.getPassword());

        Store store = queryUserStore(user);
        TokenPayload tokenPayload = buildTokenPayload(user, store != null ? store.getId() : null);
        return TokenDTO.builder()
                       .token(tokenProvider.generate(tokenPayload))
                       .build();
    }

    private Store queryUserStore(User user) {
        return storeService.lambdaQuery()
                           .eq(Store::getUserId, user.getId())
                           .one();
    }

    private User queryUserByUsername(String username) {
        return userService.lambdaQuery()
                          .eq(User::getUsername, username)
                          .one();
    }

    private static void validateUser(User user) {
        if (user == null) {
            throw new BusinessException(BizErrorCode.USER_NOT_EXISTS);
        }
    }

    private void validatePassword(String inputPassword, String actualPassword) {
        if (!passwordEncoder.matches(inputPassword, actualPassword)) {
            throw new BusinessException(BizErrorCode.PASSWORD_NOT_MATCH);
        }
    }

    private TokenPayload buildTokenPayload(User user, Long storeId) {
        return TokenPayload.builder()
                           .userId(user.getId())
                           .username(user.getUsername())
                           .role(user.getRole()
                                     .getCode())
                           .storeId(storeId)
                           .build();
    }

    @Override
    public boolean register(RegisterDTO registerDTO) {
        String username = registerDTO.getUsername();
        String password = registerDTO.getPassword();
        User user = queryUserByUsername(username);
        validateUser(user);

        userService.save(
                User.builder()
                    .username(username)
                    .password(passwordEncoder.encode(password))
                    .nickname("用户" + username)
                    .build()
        );
        return true;
    }

    @Override
    public boolean changePassword(String newPassword) {
        User user = userService.getById(UserContextHolder.getUserId());
        validateUser(user);

        user.setPassword(passwordEncoder.encode(newPassword));
        return userService.updateById(user);
    }
}