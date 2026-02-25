package com.onlineshop.framework.models.file;

/**
 * 存储配置接口
 * 所有存储类型的配置类都应该实现此接口
 * 
 * @author : Tomatos
 * @date : 2026/03/03
 */
public interface StorageConfig {
    /**
     * 验证该配置是否有效
     * 如果配置无效，应该抛出异常并说明缺失的配置项
     * 
     * @throws IllegalArgumentException 当配置无效时抛出
     */
    void validate();
}
