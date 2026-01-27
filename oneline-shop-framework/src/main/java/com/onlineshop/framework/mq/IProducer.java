package com.onlineshop.framework.mq;

import com.onlineshop.framework.common.enums.BizType;

/**
 * 订单消息生产者接口
 *
 * @author : Tomatos
 * @date : 2026/1/25
 */
public interface IProducer<T> {

    /**
     * 发送消息
     *
     * @param message 消息对象
     */
    void send(T message);

    BizType getSupportBizType();
}