package com.onlineshop.framework.interceptor;

import com.onlineshop.framework.config.WhiteListProperties;
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
import org.springframework.util.AntPathMatcher;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.List;

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
    @Autowired
    private WhiteListProperties whiteListProperties;

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws IOException {
        String requestUri = request.getRequestURI();
        if (!isHandlerMethod(handler) || isInWhiteList(requestUri)) {
            log.info("白名单请求[{}]", requestUri);
            return true;
        }

        log.info("非白名单请求[{}]", requestUri);
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
            log.error("解析token异常: {}", e.getMessage(), e);
        }

        ResponseWriteUtil.writeUnauthorized(response, "无效的Token", 401);
        return false;
    }

    private boolean isHandlerMethod(Object handler) {
        return handler instanceof HandlerMethod;
    }

    /**
     * 判断请求路径是否在白名单中
     * 支持通配符匹配，例如：/web/goods/**, /manager/auth/**
     *
     * @param requestUri 请求的 URI
     * @return 是否在白名单中
     */
    private boolean isInWhiteList(String requestUri) {
        List<String> whiteList = whiteListProperties.getWhiteList();
        if (whiteList == null || whiteList.isEmpty()) {
            return false;
        }

        for (String pattern : whiteList) {
            // 精确匹配
            if (requestUri.equals(pattern)) {
                return true;
            }

            // 通配符匹配（支持 *, **, ? 等 Ant 风格的通配符）
            if (antPathMatcher.match(pattern, requestUri)) {
                return true;
            }
        }
        return false;
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
