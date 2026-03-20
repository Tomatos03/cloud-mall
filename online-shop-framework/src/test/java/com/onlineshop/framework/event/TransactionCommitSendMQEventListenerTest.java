package com.onlineshop.framework.event;

import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class TransactionCommitSendMQEventListenerTest {

    @Test
    void shouldAsyncSendWhenPayloadIsPlainObject() {
        RecordingRocketMQTemplate rocketMQTemplate = new RecordingRocketMQTemplate();
        DefaultSendCallbackHandler defaultSendCallbackHandler = new DefaultSendCallbackHandler();
        TransactionCommitSendMQEventListener listener = new TransactionCommitSendMQEventListener(
                rocketMQTemplate,
                defaultSendCallbackHandler
        );
        TransactionCommitSendMQEvent event = new TransactionCommitSendMQEvent(
                "order_topic",
                "order_timeout_cancel",
                1001L
        );

        listener.onTransactionCommitSendMQEvent(event);

        assertEquals("order_topic:order_timeout_cancel", rocketMQTemplate.destination);
        assertEquals(1001L, rocketMQTemplate.objectPayload);
        assertSame(defaultSendCallbackHandler, rocketMQTemplate.sendCallback);
    }

    @Test
    void shouldSendMessageDirectlyWhenPayloadIsSpringMessage() {
        RecordingRocketMQTemplate rocketMQTemplate = new RecordingRocketMQTemplate();
        DefaultSendCallbackHandler defaultSendCallbackHandler = new DefaultSendCallbackHandler();
        TransactionCommitSendMQEventListener listener = new TransactionCommitSendMQEventListener(
                rocketMQTemplate,
                defaultSendCallbackHandler
        );
        Message<Long> message = MessageBuilder.withPayload(1002L)
                                              .build();
        TransactionCommitSendMQEvent event = new TransactionCommitSendMQEvent(
                "order_topic",
                "order_timeout_cancel",
                message
        );

        listener.onTransactionCommitSendMQEvent(event);

        assertEquals("order_topic:order_timeout_cancel", rocketMQTemplate.destination);
        assertEquals(1002L, rocketMQTemplate.messagePayload.getPayload());
        assertSame(defaultSendCallbackHandler, rocketMQTemplate.sendCallback);
    }

    @Test
    void shouldAsyncSendWithDelayLevelWhenEventContainsDelayLevel() {
        RecordingRocketMQTemplate rocketMQTemplate = new RecordingRocketMQTemplate();
        DefaultSendCallbackHandler defaultSendCallbackHandler = new DefaultSendCallbackHandler();
        TransactionCommitSendMQEventListener listener = new TransactionCommitSendMQEventListener(
                rocketMQTemplate,
                defaultSendCallbackHandler
        );
        Message<String> message = MessageBuilder.withPayload("ORD20260320235239770d1095")
                                                .build();
        TransactionCommitSendMQEvent event = new TransactionCommitSendMQEvent(
                "order_topic",
                "order_timeout_cancel",
                message,
                18
        );

        listener.onTransactionCommitSendMQEvent(event);

        assertEquals("order_topic:order_timeout_cancel", rocketMQTemplate.destination);
        assertNull(rocketMQTemplate.messagePayload);
        assertEquals(3000L, rocketMQTemplate.timeoutMillis);
        assertEquals(18, rocketMQTemplate.delayLevel);
        assertSame(defaultSendCallbackHandler, rocketMQTemplate.sendCallback);
    }

    private static class RecordingRocketMQTemplate extends RocketMQTemplate {
        private String destination;
        private Object objectPayload;
        private Message<?> messagePayload;
        private SendCallback sendCallback;
        private long timeoutMillis;
        private int delayLevel;

        @Override
        public void asyncSend(String destination, Object payload, SendCallback sendCallback) {
            this.destination = destination;
            this.objectPayload = payload;
            this.sendCallback = sendCallback;
        }

        @Override
        public void asyncSend(String destination, Message<?> message, SendCallback sendCallback) {
            this.destination = destination;
            this.messagePayload = message;
            this.sendCallback = sendCallback;
        }

        @Override
        public void asyncSend(String destination,
                              Message<?> message,
                              SendCallback sendCallback,
                              long timeout,
                              int delayLevel) {
            this.destination = destination;
            this.objectPayload = message;
            this.sendCallback = sendCallback;
            this.timeoutMillis = timeout;
            this.delayLevel = delayLevel;
        }
    }
}
