package com.onlineshop.framework.mq.order;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 订单消息队列配置类
 * RocketMQ 延迟级别 1-18 对应的延迟时间：
 * 1s、5s、10s、30s、1m、2m、3m、4m、5m、6m、7m、8m、9m、10m、20m、30m、1h、2h
 *
 * @author : Tomatos
 * @date : 2026/1/25
 */
@Slf4j
@ConfigurationProperties("mq.orders")
@Component
@Data
public class OrderMQConfig {
    private Integer timeoutSeconds;
    private String cancelTopic;
    private Integer autoCancelAfterMinutes = 15;
    private Integer autoConfirmAfterDays = 14;
    private String confirmTopic;
}
