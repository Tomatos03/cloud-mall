package com.onlineshop.framework.models.auth.service.impl;

import com.onlineshop.framework.models.auth.bo.ParsedToken;
import com.onlineshop.framework.models.auth.bo.TokenPayload;
import com.onlineshop.framework.models.auth.config.JwtProperties;
import com.onlineshop.framework.models.auth.service.ITokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 * @author : Tomatos
 * @date : 2025/12/31
 */
@Slf4j
@Service
public class JwtProvider implements ITokenProvider {
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
        claims.put("role", payload.getRole());
        claims.put("username", payload.getUsername());
        if (payload.getStoreId() != null) {
            claims.put("storeId", payload.getStoreId());
        }
        return claims;
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

        return getParsedToken(claims);
    }

    private @NonNull ParsedToken getParsedToken(Claims claims) {
        Long userId = claims.get("userId", Long.class);
        String username = claims.get("username", String.class);
        String role = claims.get("role", String.class);
        Long storeId = claims.get("storeId", Long.class);
        return new ParsedToken(userId, username, role, storeId);
    }
}