package com.cloudmall.framework.config;

import com.cloudmall.framework.models.file.FileProperties;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 存储配置容器
 * 当 file.storage-type=minio 时此配置生效
 * 负责创建和配置 MinioClient Bean
 *
 * @author : Tomatos
 * @date : 2026/03/03
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "file.storage-type", havingValue = "MINIO")
public class MinioStorageConfig {
    
    @Autowired
    private FileProperties fileProperties;
    
    /**
     * 创建 MinioClient Bean
     * 仅在 storage-type=MINIO 时初始化
     */
    @Bean
    public MinioClient minioClient() {
        log.debug("开始创建 MinioClient 对象...");
        
        FileProperties.Minio minioConfig = fileProperties.getMinio();
        
        MinioClient client = MinioClient.builder()
                .endpoint(minioConfig.getUrl())
                .credentials(minioConfig.getAccessKey(), minioConfig.getSecretKey())
                .build();
        
        log.info("MinioClient 创建成功");
        return client;
    }
}
