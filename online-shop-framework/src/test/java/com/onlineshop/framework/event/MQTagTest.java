package com.onlineshop.framework.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MQTagTest {

    @Test
    void shouldExposeTagForOrderTimeoutCancel() {
        assertEquals("order_timeout_cancel", MQTag.ORDER_TIMEOUT_CANCEL.getTag());
    }

    @Test
    void shouldExposeTagForSeckillOrderCreate() {
        assertEquals("seckill_order_create", MQTag.SECKILL_ORDER_CREATE.getTag());
    }

    @Test
    void shouldExposeTagForCartClear() {
        assertEquals("cart_clear", MQTag.CART_CLEAR.getTag());
    }
}
