package com.onlineshop.framework.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * 本地存储配置容器
 * 当 file.storage-type=local 时此配置生效
 *
 * @author : Tomatos
 * @date : 2026/03/03
 */
@Configuration
@ConditionalOnProperty(name = "file.storage-type", havingValue = "LOCAL", matchIfMissing = true)
public class LocalStorageConfig {
}
