package com.onlineshop.framework.application.order.creator;

import com.onlineshop.framework.models.cart.PurchaseMode;
import com.onlineshop.framework.models.order.dto.OrderCreateResultDTO;
import com.onlineshop.framework.models.order.dto.TradeDTO;

/**
 * 订单创建器接口
 *
 * @author : Tomatos
 * @date : 2026/03/10
 */
public interface IOrderCreator {
    PurchaseMode getSupportPurchaseMode();

    OrderCreateResultDTO create(TradeDTO tradeDTO);
}
