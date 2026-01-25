package com.onlineshop.framework.mq.order;

import cn.hutool.json.JSONUtil;
import com.onlineshop.framework.models.message.IMessageLogService;
import com.onlineshop.framework.models.message.MessageLog;
import com.onlineshop.framework.models.order.dto.OrderMessage;
import com.onlineshop.framework.models.order.entity.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Collection;
import java.util.List;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/1/25
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderProducer {
    private final RocketMQTemplate rocketMQTemplate;
    private final IMessageLogService messageLogService;
    private final OrderMQConfig orderMQConfig;

    /**
     * 发送订单超时取消消息
     *
     */
    public void sendOrderTimeoutCancelAfterCommit(Collection<Order> orders) {
        List<OrderMessage> orderMessages = orders.stream()
                                                 .map(this::buildOrderMessage)
                                                 .toList();

        List<MessageLog> messageLogs = orderMessages.stream()
                                                    .map(this::buildOrderTimeoutCancelMessageLog)
                                                    .toList();
        messageLogService.saveBatch(messageLogs);

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            List<Message<String>> messages = orderMessages.stream()
                                                                          .map(this::buildMessage)
                                                                          .toList();
                            rocketMQTemplate.asyncSend(
                                    orderMQConfig.getCancelTopic(),
                                    messages,
                                    new SendCallback() {
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
                                    },
                                    orderMQConfig.getTimeoutSeconds());
                        }

                        private Message<String> buildMessage(OrderMessage orderMessage) {
                            return MessageBuilder.withPayload(orderMessage.getOrderJson())
                                                 .setHeader(
                                                         RocketMQHeaders.KEYS,
                                                         orderMessage.getOrderNo()
                                                 )
                                                 .setHeader(
                                                         MessageConst.PROPERTY_DELAY_TIME_LEVEL,
                                                         orderMQConfig.getDelayLevel()
                                                 )
                                                 .build();
                        }
                    });
        }
    }

    private OrderMessage buildOrderMessage(Order order) {
        return OrderMessage.builder()
                           .orderNo(order.getNo())
                           .orderJson(JSONUtil.toJsonStr(order))
                           .build();
    }

    private MessageLog buildOrderTimeoutCancelMessageLog(OrderMessage orderMessage) {
        return MessageLog.builder()
                         .bizId(orderMessage.getOrderId())
                         .payload(orderMessage.getOrderJson())
                         .topic(orderMQConfig.getCancelTopic())
                         .build();
    }
}
