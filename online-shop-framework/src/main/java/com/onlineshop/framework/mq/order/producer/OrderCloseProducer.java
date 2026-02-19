package com.onlineshop.framework.mq.order.producer;

import com.onlineshop.framework.common.enums.BizType;
import com.onlineshop.framework.models.order.entity.Order;
import com.onlineshop.framework.mq.order.IOrderProducer;
import com.onlineshop.framework.mq.order.OrderMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Conditional;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/1/25
 */
@Slf4j
@Component
@ConditionalOnBean(RocketMQTemplate.class)
@RequiredArgsConstructor
public class OrderCloseProducer implements IOrderProducer {
    private final RocketMQTemplate rocketMQTemplate;
    private final OrderMQConfig orderMQConfig;

    @Override
    public BizType getSupportBizType() {
        return BizType.ORDER_TIMEOUT_CLOSE;
    }

    /**
     * 发送订单超时取消消息
     *
     */
    @Override
    public void send(Order order) {
        rocketMQTemplate.asyncSend(
                orderMQConfig.getCancelTopic(),
                buildMessage(order),
                sendCallback(),
                orderMQConfig.getTimeoutSeconds()
        );
    }

    private SendCallback sendCallback() {
        return new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info(
                        "订单超时取消消息发送成功，消息ID：{}",
                        sendResult.getMsgId());
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("订单超时取消消息发送失败",
                          throwable);
            }
        };
    }

    private Message<Long> buildMessage(Order order) {
        return MessageBuilder.withPayload(
                                     order.getId()
                             )
                             .setHeader(
                                     RocketMQHeaders.KEYS,
                                     order.getNo()
                             )
                             .setHeader(
                                     RocketMQHeaders.DELAY,
                                     18
                             )
                             .build();
    }
}
