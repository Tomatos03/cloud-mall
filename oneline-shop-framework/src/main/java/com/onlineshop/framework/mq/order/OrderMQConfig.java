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

    /**
     * 根据自动确认天数计算对应的 RocketMQ 延迟级别
     * RocketMQ 最大延迟为 2 小时（延迟级别 18），超过则使用最大级别
     *
     * @return RocketMQ 延迟级别（1-18）
     */
    public Integer calculateConfirmDelayLevel() {
        // RocketMQ 延迟级别映射（秒为单位）
        int[] delayLevelSeconds = {
                1,        // 级别 1: 1秒
                5,        // 级别 2: 5秒
                10,       // 级别 3: 10秒
                30,       // 级别 4: 30秒
                60,       // 级别 5: 1分钟
                120,      // 级别 6: 2分钟
                180,      // 级别 7: 3分钟
                240,      // 级别 8: 4分钟
                300,      // 级别 9: 5分钟
                360,      // 级别 10: 6分钟
                420,      // 级别 11: 7分钟
                480,      // 级别 12: 8分钟
                540,      // 级别 13: 9分钟
                600,      // 级别 14: 10分钟
                1200,     // 级别 15: 20分钟
                1800,     // 级别 16: 30分钟
                3600,     // 级别 17: 1小时
                7200      // 级别 18: 2小时
        };

        // 将天数转换为秒
        long totalSeconds = (long) autoConfirmAfterDays * 24 * 60 * 60;

        // 找到最接近的延迟级别
        int selectedLevel = 18; // 默认使用最大级别（2小时）
        for (int i = 0; i < delayLevelSeconds.length; i++) {
            if (totalSeconds <= delayLevelSeconds[i]) {
                selectedLevel = i + 1;
                break;
            }
        }

        // 如果超过 RocketMQ 最大延迟，记录警告
        if (totalSeconds > delayLevelSeconds[17]) {
            log.warn("自动确认天数 {} 超过 RocketMQ 最大延迟时间（2小时），将使用延迟级别 18（2小时）。" +
                    "建议使用定时任务方案处理超长延迟需求。", autoConfirmAfterDays);
        }

        log.debug("订单自动确认延迟: {} 天，计算得出 RocketMQ 延迟级别: {}", autoConfirmAfterDays, selectedLevel);
        return selectedLevel;
    }
}
