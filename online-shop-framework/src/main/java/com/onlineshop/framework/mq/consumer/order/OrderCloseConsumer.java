package com.onlineshop.framework.mq.consumer.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.onlineshop.framework.event.MQTag;
import com.onlineshop.framework.models.coupon.application.ICouponAppService;
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
        topic = "${mq.topic.order}",
        selectorExpression = MQTag.ORDER_TIMEOUT_CANCEL,
        consumerGroup = "${mq.group.order}"
)
@ConditionalOnProperty(name = "rocketmq.name-server")
public class OrderCloseConsumer implements RocketMQListener<String> {
    private final IOrderService orderService;
    private final ICouponAppService couponAppService;
    private static final String ORDER_CLOSE_REASON = "订单超时未支付，系统自动关闭";

    @Override
    public void onMessage(String orderNo) {
        if (orderNo == null || orderNo.isBlank()) {
            log.warn("订单超时关闭消息无效, orderNo: {}", orderNo);
            return;
        }
        log.info("收到订单超时关闭消息, orderNo: {}", orderNo);

        try {
            Order order = orderService.queryByOrderNo(orderNo);
            if (order == null) {
                log.warn("订单超时关闭消息对应订单不存在, orderNo: {}", orderNo);
                return;
            }

            order.setReason(ORDER_CLOSE_REASON);
            orderService.updateOrderStatus(order, OrderStatus.CLOSED);
            couponAppService.releaseCoupon(order.getNo());
            log.info("订单超时关闭成功, orderNo: {}", order.getNo());
        } catch (Exception e) {
            log.error("处理订单超时取消消息异常, orderNo: {}", orderNo, e);
            throw new RuntimeException("订单超时取消处理失败", e);
        }
    }
}
