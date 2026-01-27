package com.onlineshop.framework.mq.order.consumer;


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
        topic = "${mq.orders.cancel-topic}",
        consumerGroup = "${rocketmq.consumer.group}"
)
public class OrderCloseConsumer implements RocketMQListener<String> {
    private final IOrderService orderService;
    private static final String ORDER_CLOSE_REASON = "订单超时未支付，系统自动关闭";

    @Override
    public void onMessage(String message) {
        try {
            Order order = JSONUtil.toBean(message, Order.class);
            order.setReason(ORDER_CLOSE_REASON);
            if (orderService.closeOrder(order)) {
                log.debug("订单[{}]已经处理，无需关闭", order.getNo());
            } else {
                log.info("关闭超时订单: {}", order.getNo());
            }
        } catch (Exception e) {
            log.error("处理订单超时取消消息异常: {}", message, e);
            throw new RuntimeException("订单超时取消处理失败", e);
        }
    }
}
