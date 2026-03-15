package com.onlineshop.framework.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 *
 *
 * @author : Tomatos
 * @date : 2025/12/25
 */
@ConfigurationProperties("auth")
@Configuration
@Data
public class WhiteListProperties {
    private List<String> whiteList;
}
