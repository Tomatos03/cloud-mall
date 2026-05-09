package com.cloudmall.framework.event;

import lombok.Getter;

/**
 * 事务提交后发送 MQ 的通用事件
 *
 * @author : Tomatos
 * @date : 2026/3/15
 */
@Getter
public class TransactionCommitSendMQEvent {
    private final String topic;
    private final String tag;
    private final Object message;
    private final Integer delayLevel;

    public TransactionCommitSendMQEvent(String topic, String tag, Object message) {
        this(topic, tag, message, null);
    }

    public TransactionCommitSendMQEvent(String topic, String tag, Object message, Integer delayLevel) {
        this.topic = topic;
        this.tag = tag;
        this.message = message;
        this.delayLevel = delayLevel;
    }
}
