package com.cloudmall.framework.config;

import com.cloudmall.framework.config.mybatisPlus.MybatisPlusConfig;
import com.cloudmall.framework.security.SecurityBean;
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
    SecurityBean.class,
    WebConfig.class
})
public class FrameworkConfig {
}