package com.onlineshop.framework.event;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/1/29
 */
public enum BizEvent {
    /**
     * 购物车相关事件
     */
    CLEAR_CART, // 清空购物车

    /**
     * 商品相关事件
     */
    SYNC_GOODS_TO_ES,    // 同步商品信息到ES
    DEL_GOODS_FROM_ES,   // 从ES删除商品

    /**
     * 订单相关事件
     */
    ORDER_TIMEOUT_CANCEL // 订单超时自动取消
}
