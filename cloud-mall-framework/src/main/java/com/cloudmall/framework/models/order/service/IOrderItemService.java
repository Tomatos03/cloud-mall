package com.cloudmall.framework.models.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudmall.framework.models.order.entity.OrderItem;

import java.util.List;

/**
 * 订单明细服务接口
 *
 * @author : Tomatos
 * @date : 2025/12/24
 */
public interface IOrderItemService extends IService<OrderItem> {
    
    /**
     * 根据订单ID查询订单明细列表
     *
     * @param orderId 订单ID
     * @return 订单明细列表
     */
    List<OrderItem> listByOrderId(Long orderId);

    /**
     * 根据多个订单ID批量查询订单明细列表
     *
     * @param orderIds 订单ID列表
     * @return 订单明细列表
     */
    List<OrderItem> listByOrderIds(List<Long> orderIds);

    /**
     * 批量保存订单明细
     *
     * @param orderItems 订单明细列表
     * @return 是否成功
     */
    boolean saveBatchItems(List<OrderItem> orderItems);
}