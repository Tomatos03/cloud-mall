package com.onlineshop.framework.mq.order;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/1/25
 */
@ConfigurationProperties("mq.orders")
@Component
@Data
public class OrderMQConfig {
    private Integer timeoutSeconds;
    private String cancelTopic;
    private Integer delayLevel;
}
