package com.cloudmall.framework.event;

/**
 * MQ 业务动作定义
 *
 * @author : Tomatos
 * @date : 2026/3/15
 */
public final class MQTag {
    public static final String ORDER_TIMEOUT_CANCEL = "order_timeout_cancel";
    public static final String SECKILL_ORDER_CREATE = "seckill_order_create";
    public static final String CART_CLEAR = "cart_clear";
    public static final String GOODS_SYNC_TO_ES = "goods_sync_es";
    public static final String GOODS_DELETE_FROM_ES = "goods_delete_es";
    public static final String COUPON_EXPIRE = "coupon_expire";

    private MQTag() {
        throw new AssertionError("Cannot instantiate utility class");
    }
}
