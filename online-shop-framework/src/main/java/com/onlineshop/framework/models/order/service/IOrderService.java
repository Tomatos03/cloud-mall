package com.onlineshop.framework.models.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.onlineshop.framework.models.order.entity.Order;
import com.onlineshop.framework.models.order.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单领域服务接口
 * 仅负责 order 表相关操作
 */
public interface IOrderService extends IService<Order> {
    Order queryByOrderNo(String orderNo);

    Order queryUserOrderByOrderNo(String orderNo);

    Order queryByOrderNoAndStoreIds(String orderNo, List<Long> storeIds);

    List<Order> querySubOrders(Long parentId);

    boolean updateOrderStatus(Order order, OrderStatus newStatus);

    void savePayOrder(Order payOrder);

    void saveOrders(List<Order> orders);

    List<Order> queryShippedOrders();

    List<Order> queryTimeoutCreatedOrders(LocalDateTime deadline);
}
