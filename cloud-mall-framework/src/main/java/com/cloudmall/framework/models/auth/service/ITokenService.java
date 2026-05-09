package com.cloudmall.framework.models.auth.service;

import com.cloudmall.framework.models.auth.bo.ParsedToken;
import com.cloudmall.framework.models.auth.bo.TokenPayload;

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
