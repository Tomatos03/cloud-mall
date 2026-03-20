package com.onlineshop.framework.mq.consumer.order;

import org.apache.rocketmq.spring.core.RocketMQListener;
import org.junit.jupiter.api.Test;

import com.onlineshop.framework.models.order.entity.Order;
import com.onlineshop.framework.models.order.enums.OrderStatus;
import com.onlineshop.framework.models.order.service.IOrderService;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderCloseConsumerTest {

    @Test
    void onMessage_shouldQueryByOrderNoAndCloseOrder() {
        String orderNo = "ORDER_1001";
        IOrderService orderService = mock(IOrderService.class);
        Order order = new Order();
        order.setNo(orderNo);
        when(orderService.queryByOrderNo(orderNo)).thenReturn(order);
        when(orderService.updateOrderStatus(order, OrderStatus.CLOSED)).thenReturn(true);

        RocketMQListener<String> listener = new OrderCloseConsumer(orderService);

        assertDoesNotThrow(() -> listener.onMessage(orderNo));
        verify(orderService).queryByOrderNo(orderNo);
        verify(orderService).updateOrderStatus(order, OrderStatus.CLOSED);
    }

    @Test
    void onMessage_shouldIgnoreWhenOrderNotFound() {
        String orderNo = "ORDER_1002";
        IOrderService orderService = mock(IOrderService.class);
        when(orderService.queryByOrderNo(orderNo)).thenReturn(null);

        RocketMQListener<String> listener = new OrderCloseConsumer(orderService);

        assertDoesNotThrow(() -> listener.onMessage(orderNo));
        verify(orderService).queryByOrderNo(orderNo);
        verify(orderService, never()).updateOrderStatus(any(Order.class), eq(OrderStatus.CLOSED));
    }
}
