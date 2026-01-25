package com.onlineshop.framework.models.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.onlineshop.framework.models.order.entity.OrderItem;

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
     * 批量保存订单明细
     *
     * @param orderItems 订单明细列表
     * @return 是否成功
     */
    boolean saveBatchItems(List<OrderItem> orderItems);
}