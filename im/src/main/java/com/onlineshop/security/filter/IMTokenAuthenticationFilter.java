package com.onlineshop.security.filter;

import com.onlineshop.framework.models.auth.bo.ParsedToken;
import com.onlineshop.framework.models.auth.service.ITokenService;
import com.onlineshop.framework.security.AuthUser;
import com.onlineshop.framework.utils.AuthUserUtils;
import com.onlineshop.framework.utils.ResponseWriteUtil;
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
public class IMTokenAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private ITokenService tokenService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String requestUri = request.getRequestURI();
        log.info("Method[{}], URI: {}", request.getMethod(), requestUri);
        try {
            String token;
            ParsedToken parsedToken;
            if (
                    (token = getTokenFromRequest(request)) != null
                            && (parsedToken = tokenService.parse(token)) != null
            ) {
                AuthUser authUser = convertToAuthUser(parsedToken);
                UsernamePasswordAuthenticationToken authenticatedToken =
                        new UsernamePasswordAuthenticationToken(
                        authUser,
                        "",
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
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return request.getParameter("token");
    }

    private AuthUser convertToAuthUser(ParsedToken parsedToken) {
        AuthUser authUser = new AuthUser(
                parsedToken.getUsername(),
                "",
                Collections.emptyList()
        );
        authUser.setUserId(parsedToken.getUserId());
        authUser.setStoreId(parsedToken.getStoreId());
        return authUser;
    }
}