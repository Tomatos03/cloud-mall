package com.cloudmall.framework.models.file;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文件存储类型枚举
 *
 * @author : Tomatos
 * @date : 2026/03/03
 */
@AllArgsConstructor
@Getter
public enum StorageType {
    /**
     * 本地存储
     */
    LOCAL("local"),
    
    /**
     * MinIO 对象存储
     */
    MINIO("minio");
    
    // 未来扩展示例：
    // OSS("oss"),      // 阿里云对象存储
    // COS("cos"),      // 腾讯云对象存储
    // S3("s3")         // AWS S3
    
    /**
     * 配置字段名称
     */
    private final String code;
}