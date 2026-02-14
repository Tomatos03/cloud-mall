package com.onlineshop.framework.models.auth.service;

import com.onlineshop.framework.models.auth.bo.ParsedToken;
import com.onlineshop.framework.models.auth.bo.TokenPayload;

/**
 *
 *
 * @author : Tomatos
 * @date : 2025/12/31
 */
public interface ITokenService {
    String generate(TokenPayload payload);

    ParsedToken parse(String token);
}
