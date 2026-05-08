package com.onlineshop.framework.models.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.onlineshop.framework.models.order.dto.OrderParamsDTO;
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

    Order queryMyOrder(String orderNo);

    Order queryStoreOrder(String orderNo, Long storeId);

    Order queryCancelableOrder(String orderNo);

    List<Order> querySubOrders(Long parentId);

    void updateOrderStatus(String orderNo, OrderStatus newStatus);

    void updateOrderStatus(Order order, OrderStatus newStatus);

    void saveOrders(List<Order> orders);

    List<Order> queryShippedOrders();

    List<Order> queryTimeoutOrders(LocalDateTime deadline);

    IPage<Order> pageQueryOrders(OrderParamsDTO queryDTO);
}
