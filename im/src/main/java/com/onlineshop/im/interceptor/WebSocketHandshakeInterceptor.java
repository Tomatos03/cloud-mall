package com.onlineshop.im.interceptor;

import com.onlineshop.framework.models.auth.bo.ParsedToken;
import com.onlineshop.framework.models.auth.service.ITokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * WebSocket握手拦截器
 * 在WebSocket握手阶段从URL查询参数中提取token并进行认证
 *
 * @author : Tomatos
 * @date : 2026/2/17
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {
    private final ITokenService tokenService;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        try {
            String query = request.getURI()
                                  .getQuery();
            if (query == null || !query.contains("token=")) {
                log.warn("WebSocket连接请求中未找到token参数");
                return false;
            }

            // 提取token值
            String token = extractTokenFromQuery(query);
            if (token == null || token.isEmpty()) {
                log.warn("WebSocket token参数为空");
                return false;
            }

            log.info("WebSocket握手：开始解析token...");

            // 解析token
            ParsedToken parsedToken = tokenService.parse(token);
            if (parsedToken == null) {
                log.warn("WebSocket token解析失败");
                return false;
            }

            log.info("WebSocket握手：token解析成功，userId: {}, username: {}", parsedToken.getUserId(),
                     parsedToken.getUsername());

            attributes.put("userId", parsedToken.getUserId());
            log.info("WebSocket握手：认证成功");
            return true;
        } catch (Exception e) {
            log.error("WebSocket握手认证异常:", e);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                               @Nullable Exception exception) {
    }


    /**
     * 从查询字符串中提取token值
     *
     * @param query 查询字符串，如 "token=abc123&other=value"
     * @return token值，如果不存在则返回null
     */
    private String extractTokenFromQuery(String query) {
        if (query == null) return null;

        String[] params = query.split("&");
        for (String param : params) {
            if (param.startsWith("token=")) {
                String token = param.substring("token=".length());
                try {
                    // URL decode
                    return URLDecoder.decode(token, StandardCharsets.UTF_8);
                } catch (Exception e) {
                    log.error("token URL解码失败", e);
                    return null;
                }
            }
        }
        return null;
    }
}
