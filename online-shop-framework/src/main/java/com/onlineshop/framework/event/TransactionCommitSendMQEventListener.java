package com.onlineshop.framework.event;

import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.messaging.Message;
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
@ConditionalOnBean(RocketMQTemplate.class)
public class TransactionCommitSendMQEventListener {
    private final RocketMQTemplate rocketMQTemplate;
    private final DefaultSendCallbackHandler defaultSendCallbackHandler;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTransactionCommitSendMQEvent(TransactionCommitSendMQEvent event) {
        String destination = event.getTopic() + ":" + event.getTag();
        Object message = event.getMessage();
        if (message instanceof Message<?> springMessage) {
            rocketMQTemplate.asyncSend(destination, springMessage, defaultSendCallbackHandler);
            return;
        }
        rocketMQTemplate.asyncSend(destination, message, defaultSendCallbackHandler);
    }
}
