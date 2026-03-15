package com.onlineshop.framework.event;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.springframework.stereotype.Component;

/**
 * 事务提交消息发送默认回调处理器
 *
 * @author : Tomatos
 * @date : 2026/3/15
 */
@Slf4j
@Component
public class DefaultSendCallbackHandler implements SendCallback {

    @Override
    public void onSuccess(SendResult sendResult) {
        String messageId = sendResult == null ? "" : sendResult.getMsgId();
        log.info("事务提交后发送 MQ 成功, messageId: {}", messageId);
    }

    @Override
    public void onException(Throwable throwable) {
        log.error("事务提交后发送 MQ 失败", throwable);
    }
}
