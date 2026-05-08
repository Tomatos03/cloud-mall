package com.onlineshop.framework.models.order.application;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlineshop.framework.models.cart.PurchaseMode;
import com.onlineshop.framework.models.order.dto.OrderCancelDTO;
import com.onlineshop.framework.models.order.dto.OrderCreateResultDTO;
import com.onlineshop.framework.models.order.dto.OrderParamsDTO;
import com.onlineshop.framework.models.order.dto.TradeDTO;
import com.onlineshop.framework.models.order.entity.Order;
import com.onlineshop.framework.models.order.vo.OrderAggregateVO;
import com.onlineshop.framework.models.order.vo.OrderVO;

/**
 * 订单应用服务接口
 * 负责订单相关业务流程编排
 */
public interface IOrderAppService {
    OrderCreateResultDTO createOrder(TradeDTO tradeDTO, PurchaseMode purchaseMode);

    IPage<OrderAggregateVO> pageQueryOrdersForClient(OrderParamsDTO queryDTO);

    boolean queryPaymentStatus(String orderNo);

    void deductInventory(String orderNo);

    OrderAggregateVO queryOrderDetail(String orderNo);

    Order queryOrder(Long orderId);

    void cancelOrder(OrderCancelDTO cancelDTO);

    void finishOrder(String orderNo);

    IPage<OrderVO> pageQueryOrdersForAdmin(OrderParamsDTO queryDTO);

    boolean shipOrder(String orderNo);

    OrderVO queryOrderComment(String orderNo);

    boolean autoReceiveOrder(Order order);

    int autoReceiveShippedOrders();

    int closeTimeoutOrders();

    void refundOrder(String orderNo);
}
