package com.onlineshop.framework.mq.order.consumer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.onlineshop.framework.models.order.entity.Order;
import com.onlineshop.framework.models.order.enums.OrderStatus;
import com.onlineshop.framework.models.order.service.IOrderService;

/**
 * 订单超时取消消费者
 *
 * @author : Tomatos
 * @date : 2026/1/25
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = "${mq.orders.cancel-topic}",
        consumerGroup = "${rocketmq.consumer.group}"
)
@ConditionalOnProperty(name = "rocketmq.name-server")
public class OrderCloseConsumer implements RocketMQListener<Long> {
    private final IOrderService orderService;
    private static final String ORDER_CLOSE_REASON = "订单超时未支付，系统自动关闭";

    @Override
    public void onMessage(Long orderId) {
        try {
            Order order = orderService.getById(orderId);
            if (order == null) {
                log.warn("订单超时关闭消息对应订单不存在, orderId: {}", orderId);
                return;
            }

            order.setReason(ORDER_CLOSE_REASON);
            boolean closed = orderService.updateOrderStatus(order, OrderStatus.CLOSED);
            if (!closed) {
                log.info("订单状态不允许关闭或已被处理, orderId: {}, orderNo: {}", orderId, order.getNo());
            }
        } catch (Exception e) {
            log.error("处理订单超时取消消息异常: {}", e.getMessage());
            throw new RuntimeException("订单超时取消处理失败", e);
        }
    }
}
