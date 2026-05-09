package com.cloudmall.framework.event;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MQ 模块 Topic 配置
 *
 * @author : Tomatos
 * @date : 2026/3/15
 */
@Data
@Component
@ConfigurationProperties(prefix = "mq.topic")
public class MQTopicProperties {
    private String order;
    private String seckill;
    private String cart;
    private String goods;
    private String coupon;
}
