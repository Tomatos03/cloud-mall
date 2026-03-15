package com.onlineshop.framework.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 事务提交后发送 MQ 的通用事件
 *
 * @author : Tomatos
 * @date : 2026/3/15
 */
@Getter
@RequiredArgsConstructor
public class TransactionCommitSendMQEvent {
    private final String topic;
    private final String tag;
    private final Object message;
}
