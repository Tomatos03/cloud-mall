package com.cloudmall.framework.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 *
 *
 * @author : Tomatos
 * @date : 2025/12/18
 */
@Configuration
@ConditionalOnProperty(name = "file.storage-type", havingValue = "LOCAL", matchIfMissing = true)
public class WebConfig implements WebMvcConfigurer {
    @Value("${file.upload-dir}")
    private String uploadDir;
    @Value("${file.endpoint}")
    private String uploadApiPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(uploadApiPrefix + "/**")
                .addResourceLocations("file:" + uploadDir);
    }
}