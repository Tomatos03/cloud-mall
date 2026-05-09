package com.cloudmall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 *
 *
 * @author : Tomatos
 * @date : 2025/12/17
 */
@EnableCaching
@SpringBootApplication
public class CloudMallManageApplication {
    public static void main(String[] args) {
        SpringApplication.run(CloudMallManageApplication.class, args);
    }
}
