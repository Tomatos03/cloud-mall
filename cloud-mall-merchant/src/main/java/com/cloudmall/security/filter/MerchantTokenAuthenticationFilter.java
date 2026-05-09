package com.cloudmall.security.filter;

import com.cloudmall.framework.models.auth.bo.ParsedToken;
import com.cloudmall.framework.models.auth.enums.AccountType;
import com.cloudmall.framework.models.auth.service.ITokenService;
import com.cloudmall.framework.security.AuthUser;
import com.cloudmall.framework.utils.AuthUserUtils;
import com.cloudmall.framework.utils.ResponseWriteUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Token认证过滤器
 * 在token解析成功后，从token中提取角色并转换为资源代码权限
 *
 * @author : Tomatos
 * @date : 2026/2/3
 */
@Slf4j
@Component
public class MerchantTokenAuthenticationFilter extends OncePerRequestFilter {
    private static final String MERCHANT_API_PREFIX = "/merchant";

    @Autowired
    private ITokenService tokenService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        return !requestUri.equals(MERCHANT_API_PREFIX) && !requestUri.startsWith(MERCHANT_API_PREFIX + "/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String requestUri = request.getRequestURI();
        log.info("{} {}", request.getMethod(), requestUri);
        try {
            String token;
            ParsedToken parsedToken;
            if (
                    (token = getTokenFromRequest(request)) != null
                            && (parsedToken = tokenService.parse(token)) != null
            ) {
                if (!AccountType.MERCHANT.getCode().equals(parsedToken.getAccountType())) {
                    ResponseWriteUtil.writeUnauthorized(response, "未授权");
                    return;
                }

                AuthUser authUser = convertToAuthUser(parsedToken);
                UsernamePasswordAuthenticationToken authenticatedToken =
                        new UsernamePasswordAuthenticationToken(
                        authUser,
                        null,
                        Collections.emptyList()
                );
                AuthUserUtils.setAuthentication(authenticatedToken);
            }
        } catch (Exception e) {
            log.error("解析Token异常: {}", e.getMessage());
            ResponseWriteUtil.writeUnauthorized(response, "Token过期");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private AuthUser convertToAuthUser(ParsedToken parsedToken) {
        AuthUser authUser = new AuthUser(
                parsedToken.getUsername(),
                "",
                Collections.emptyList()
        );
        authUser.setUserId(parsedToken.getUserId());
        authUser.setStoreId(parsedToken.getStoreId());
        authUser.setCurrentAccountType(parsedToken.getAccountType());
        return authUser;
    }
}
