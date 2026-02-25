package com.onlineshop.framework.models.file;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 统一的文件存储配置
 * 支持多种存储类型（Local、MinIO等），通过 storage-type 属性选择
 *
 * @author : Tomatos
 * @date : 2026/03/03
 */
@Data
@Component
@ConfigurationProperties(prefix = "file")
public class FileProperties {
    private StorageType storageType = StorageType.LOCAL;
    
    /**
     * 文件上传通用配置
     */
    private Upload upload = new Upload();
    
    /**
     * 本地存储配置
     */
    private Local local = new Local();
    
    /**
     * MinIO 存储配置
     */
    private Minio minio = new Minio();

    @PostConstruct
    public void validate() {
        validateActiveStorageConfig();
    }
    
    /**
     * 验证当前激活的存储类型配置不为空
     */
    private void validateActiveStorageConfig() {
        Object config = getConfigByStorageType(storageType);
        
        if (config == null) {
            throw new IllegalArgumentException(
                String.format(
                    "Missing required config for storage-type '%s'. " +
                    "Please configure file.%s",
                    storageType.name().toLowerCase(),
                    storageType.getCode()
                )
            );
        }
        
        // 使用接口统一验证，每个配置类自己负责验证自己
        if (config instanceof StorageConfig storageConfig) {
            storageConfig.validate();
        }
    }
    
    /**
     * 根据存储类型获取对应的配置对象
     */
    private Object getConfigByStorageType(StorageType type) {
        return switch (type) {
            case LOCAL -> this.local;
            case MINIO -> this.minio;
        };
    }
    
    /**
     * 文件上传通用配置
     */
    @Data
    public static class Upload {
        /**
         * 最大文件大小（字节），默认 5MB
         */
        private Long maxSize = 5 * 1024 * 1024L;
        
        /**
         * 允许的文件扩展名
         */
        private List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png");
        
        /**
         * 允许的 MIME 类型
         */
        private List<String> allowedMimeTypes = Arrays.asList("image/jpeg", "image/png");
    }
    
    /**
     * 本地存储配置
     */
    @Data
    public static class Local implements StorageConfig {
        /**
         * 本地上传目录
         */
        private String uploadDir;
        
        /**
         * 文件访问端点
         */
        private String endpoint;
        
        @Override
        public void validate() {
            if (!hasValue(uploadDir)) {
                throw new IllegalArgumentException(
                    "Missing required config: file.local.upload-dir"
                );
            }
            if (!hasValue(endpoint)) {
                throw new IllegalArgumentException(
                    "Missing required config: file.local.endpoint"
                );
            }
        }
        
        private static boolean hasValue(String value) {
            return value != null && !value.trim().isEmpty();
        }
    }
    
    /**
     * MinIO 存储配置
     */
    @Data
    public static class Minio implements StorageConfig {
        /**
         * MinIO 服务地址
         */
        private String url;
        
        /**
         * 访问密钥（用户名）
         */
        private String accessKey;
        
        /**
         * 秘密密钥（密码）
         */
        private String secretKey;
        
        /**
         * 存储桶名称
         */
        private String bucketName;
        
        @Override
        public void validate() {
            if (!hasValue(url)) {
                throw new IllegalArgumentException(
                    "Missing required config: file.minio.url"
                );
            }
            if (!hasValue(accessKey)) {
                throw new IllegalArgumentException(
                    "Missing required config: file.minio.access-key"
                );
            }
            if (!hasValue(secretKey)) {
                throw new IllegalArgumentException(
                    "Missing required config: file.minio.secret-key"
                );
            }
            if (!hasValue(bucketName)) {
                throw new IllegalArgumentException(
                    "Missing required config: file.minio.bucket-name"
                );
            }
        }
        
        private static boolean hasValue(String value) {
            return value != null && !value.trim().isEmpty();
        }
    }
}
