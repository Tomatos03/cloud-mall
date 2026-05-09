package com.cloudmall.framework.models.auth.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.cloudmall.framework.models.auth.bo.ParsedToken;
import com.cloudmall.framework.models.auth.bo.TokenPayload;
import com.cloudmall.framework.models.auth.config.JwtProperties;
import com.cloudmall.framework.models.auth.service.ITokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
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

        return Jwts.builder()
                   .claims(buildClaims(payload))
                   .subject(payload.getUsername())
                   .issuedAt(new Date(now))
                   .expiration(new Date(now + jwtProperties.getExpire()))
                   .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                   .compact();
    }

    private Map<String, Object> buildClaims(TokenPayload payload) {
        return BeanUtil.beanToMap(payload, false, true);
    }

    @Override
    public ParsedToken parse(String token) {
        String secret = jwtProperties.getSecret();
        Claims claims = Jwts.parser()
                            .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
                            .build()
                            .parseSignedClaims(token)
                            .getPayload();

        return buildParsedToken(claims);
    }

    private @NonNull ParsedToken buildParsedToken(Claims claims) {
        return BeanUtil.toBean(claims, ParsedToken.class);
    }
}