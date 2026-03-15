package com.onlineshop.framework.models.order.service.impl;

import org.junit.jupiter.api.Test;

import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.exception.BizException;
import com.onlineshop.framework.models.order.entity.Order;
import com.onlineshop.framework.models.order.enums.OrderStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class OrderServiceTest {

    @Test
    void updateOrderStatus_shouldQueryOrderByOrderNoThenDelegateToOriginalMethod() {
        String orderNo = "ORDER_1001";
        OrderStatus newStatus = OrderStatus.PAID;
        OrderService orderService = spy(new OrderService());
        Order order = new Order();
        order.setNo(orderNo);

        doReturn(order).when(orderService)
                       .queryByOrderNo(orderNo);
        doReturn(true).when(orderService)
                      .updateOrderStatus(order, newStatus);

        boolean updated = orderService.updateOrderStatus(orderNo, newStatus);

        assertTrue(updated);
        verify(orderService).queryByOrderNo(orderNo);
        verify(orderService).updateOrderStatus(order, newStatus);
    }

    @Test
    void updateOrderStatus_shouldThrowWhenOrderNotFound() {
        String orderNo = "ORDER_1002";
        OrderService orderService = spy(new OrderService());

        doReturn(null).when(orderService)
                      .queryByOrderNo(orderNo);

        BizException exception = assertThrows(BizException.class,
                                              () -> orderService.updateOrderStatus(orderNo, OrderStatus.PAID));

        assertEquals(BizErrorCode.ORDER_NOT_EXIST, exception.getBizErrorCode());
        verify(orderService).queryByOrderNo(orderNo);
    }
}
