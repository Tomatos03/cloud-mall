package com.onlineshop.framework.interceptor;

import com.onlineshop.framework.models.auth.bo.ParsedToken;
import com.onlineshop.framework.models.auth.service.ITokenProvider;
import com.onlineshop.framework.utils.ResponseWriteUtil;
import com.onlineshop.framework.utils.context.UserContext;
import com.onlineshop.framework.utils.context.UserContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * Token 拦截器
 * 支持从配置文件读取白名单，支持通配符匹配
 *
 * @author : Tomatos
 * @date : 2025/12/18
 */
@Slf4j
@Component
public class TokenInterceptor implements HandlerInterceptor {
    @Autowired
    private ITokenProvider tokenProvider;

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws IOException {
        String requestUri = request.getRequestURI();
        if (!isHandlerMethod(handler)) {
            return true;
        }

        try {
            String token;
            ParsedToken parsedToken;
            if (
                    ((token = getTokenFromRequest(request)) != null)
                    && (parsedToken = tokenProvider.parse(token)) != null
            ) {
                UserContext userContext = convertUserContext(parsedToken);
                UserContextHolder.setUserContext(userContext);
                return true;
            }
        } catch (Exception e) {
            log.error("解析token异常: {}", e.getMessage());
        }

        log.info("未授权的请求[{}]", requestUri);
        ResponseWriteUtil.writeUnauthorized(response, "无效的Token", 401);
        return false;
    }

    private boolean isHandlerMethod(Object handler) {
        return handler instanceof HandlerMethod;
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private UserContext convertUserContext(ParsedToken parsedToken) {
        return UserContext.builder()
                          .id(parsedToken.getUserId())
                          .username(parsedToken.getUsername())
                          .roleCode(parsedToken.getRole())
                          .storeId(parsedToken.getStoreId())
                          .build();
    }
}
