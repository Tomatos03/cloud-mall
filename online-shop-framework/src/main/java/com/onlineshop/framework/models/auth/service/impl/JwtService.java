package com.onlineshop.framework.models.auth.service.impl;

import com.onlineshop.framework.models.auth.bo.ParsedToken;
import com.onlineshop.framework.models.auth.bo.TokenPayload;
import com.onlineshop.framework.models.auth.config.JwtProperties;
import com.onlineshop.framework.models.auth.service.ITokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 *
 * @author : Tomatos
 * @date : 2025/12/31
 */
@Slf4j
@Service
public class JwtService implements ITokenService {
    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public String generate(TokenPayload payload) {
        String secret = jwtProperties.getSecret();
        long now = System.currentTimeMillis();

        Map<String, Object> claims = buildClaims(payload);
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());

        return Jwts.builder()
                   .claims(claims)
                   .subject(payload.getUsername())
                   .issuedAt(new Date(now))
                   .expiration(new Date(now + jwtProperties.getExpire()))
                   .signWith(key)
                   .compact();
    }

    private Map<String, Object> buildClaims(TokenPayload payload) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", payload.getUserId());
        claims.put("roles", payload.getRoles());
        claims.put("username", payload.getUsername());
        if (payload.getStoreId() != null) {
            claims.put("storeId", payload.getStoreId());
        }
        return claims;
    }

    private @NonNull ParsedToken buildParsedToken(Claims claims) {
        Long userId = claims.get("userId", Long.class);
        String username = claims.get("username", String.class);
        List<String> roles = extractRoles(claims);
        Long storeId = claims.get("storeId", Long.class);
        return new ParsedToken(userId, username, roles, storeId);
    }

    @Override
    public ParsedToken parse(String token) {
        String secret = jwtProperties.getSecret();

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());

        Claims claims = Jwts.parser()
                            .verifyWith(key)
                            .build()
                            .parseSignedClaims(token)
                            .getPayload();

        return buildParsedToken(claims);
    }

    /**
     * 从 JWT claims 中提取 roles 列表
     * JWT 库对泛型 List 处理需要特殊转换
     *
     * @param claims JWT 声明
     * @return roles 列表，如果不存在或为 null 则返回空列表
     */
    private List<String> extractRoles(Claims claims) {
        try {
            Object rolesObj = claims.get("roles");
            if (rolesObj == null) {
                return Collections.emptyList();
            }
            
            // JWT 解析后通常为 List<LinkedHashMap> 或 List<Object>
            if (rolesObj instanceof List<?> rolesList) {
                return rolesList.stream()
                               .map(Object::toString)
                               .toList();
            }
            
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("提取 roles 失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}