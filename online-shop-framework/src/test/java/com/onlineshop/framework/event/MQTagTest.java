package com.onlineshop.framework.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MQTagTest {

    @Test
    void shouldExposeTagForOrderTimeoutCancel() {
        assertEquals("order_timeout_cancel", MQTag.ORDER_TIMEOUT_CANCEL);
    }

    @Test
    void shouldExposeTagForSeckillOrderCreate() {
        assertEquals("seckill_order_create", MQTag.SECKILL_ORDER_CREATE);
    }

    @Test
    void shouldExposeTagForCartClear() {
        assertEquals("cart_clear", MQTag.CART_CLEAR);
    }

    @Test
    void shouldExposeTagForGoodsSyncToEs() {
        assertEquals("goods_sync_es", MQTag.GOODS_SYNC_TO_ES);
    }

    @Test
    void shouldExposeTagForGoodsDeleteFromEs() {
        assertEquals("goods_delete_es", MQTag.GOODS_DELETE_FROM_ES);
    }
}
