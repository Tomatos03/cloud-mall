package com.cloudmall.framework.event;

import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 事务提交后 MQ 发送监听器
 *
 * @author : Tomatos
 * @date : 2026/3/15
 */
@Component
@RequiredArgsConstructor
public class TransactionCommitSendMQEventListener {
    private static final long DEFAULT_SEND_TIMEOUT_MILLIS = 3000L;

    private final RocketMQTemplate rocketMQTemplate;
    private final DefaultSendCallbackHandler defaultSendCallbackHandler;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTransactionCommitSendMQEvent(TransactionCommitSendMQEvent event) {
        // https://github.com/apache/rocketmq-spring/wiki/%E5%B8%B8%E8%A7%81%E9%97%AE%E9%A2%98
        // desination规定格式: topicName:tagName
        String destination = String.format("%s:%s", event.getTopic(), event.getTag());
        Integer delayLevel = event.getDelayLevel();
        Object message = event.getMessage();
        if (message instanceof Message<?> springMessage) {
            asyncSendMessage(destination, springMessage, delayLevel);
            return;
        }

        if (delayLevel != null && delayLevel > 0) {
            Message<?> springMessage = MessageBuilder.withPayload(message)
                                                     .build();
            asyncSendMessage(destination, springMessage, delayLevel);
            return;
        }

        rocketMQTemplate.asyncSend(destination, message, defaultSendCallbackHandler);
    }

    private void asyncSendMessage(String destination, Message<?> springMessage, Integer delayLevel) {
        if (delayLevel != null && delayLevel > 0) {
            rocketMQTemplate.asyncSend(
                    destination,
                    springMessage,
                    defaultSendCallbackHandler,
                    DEFAULT_SEND_TIMEOUT_MILLIS,
                    delayLevel
            );
            return;
        }
        rocketMQTemplate.asyncSend(destination, springMessage, defaultSendCallbackHandler);
    }
}
