package com.onlineshop.framework.config;

import com.onlineshop.framework.config.mybatisPlus.MybatisPlusConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 *
 *
 * @author : Tomatos
 * @date : 2025/12/18
 */
@AutoConfiguration
@Import({
    MybatisPlusConfig.class,
    SecurityConfig.class,
    WebConfig.class
})
public class FrameworkConfig {
}