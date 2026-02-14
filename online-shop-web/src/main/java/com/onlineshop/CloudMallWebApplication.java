package com.onlineshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 *
 *
 * @author : Tomatos
 * @date : 2025/12/18
 */
@EnableCaching
@SpringBootApplication
public class CloudMallWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(CloudMallWebApplication.class, args);
    }
}
