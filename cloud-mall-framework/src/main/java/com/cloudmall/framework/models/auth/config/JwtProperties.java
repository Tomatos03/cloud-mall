package com.cloudmall.framework.models.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置属性类
 *
 * @author : Tomatos
 * @date : 2025/12/31
 */
@ConfigurationProperties(prefix = "jwt")
@Component
@Data
public class JwtProperties {
    
    /**
     * JWT 签名密钥
     * 至少需要32个字符以满足 HS256 要求
     */
    private String secret;
    
    /**
     * JWT 过期时间（毫秒）
     * 默认值：86400000（24小时）
     */
    private Long expire;
}