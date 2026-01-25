package com.onlineshop.framework.mq.order;


import cn.hutool.json.JSONUtil;
import com.onlineshop.framework.models.order.entity.Order;
import com.onlineshop.framework.models.order.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

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
        topic = "order_timeout_cancel_topic",
        consumerGroup = "order_timeout_cancel_group",
        selectorExpression = "*"
)
public class OrderTimeoutCancelConsumer implements RocketMQListener<String> {
    private final IOrderService orderService;

    @Override
    public void onMessage(String message) {
        try {
            log.info("开始处理订单超时取消消息: {}", message);
            Order order = JSONUtil.toBean(message, Order.class);
            boolean result = orderService.cancelOrder(order.getNo());
        } catch (Exception e) {
            log.error("处理订单超时取消消息异常: {}", message, e);
            throw new RuntimeException("订单超时取消处理失败", e);
        }
    }
}
