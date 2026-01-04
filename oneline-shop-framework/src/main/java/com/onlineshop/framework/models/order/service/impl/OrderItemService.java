package com.onlineshop.framework.models.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineshop.framework.models.order.mapper.OrderItemMapper;
import com.onlineshop.framework.models.order.entity.OrderItem;
import com.onlineshop.framework.models.order.service.IOrderItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 订单明细服务实现类
 *
 * @author : Tomatos
 * @date : 2025/12/24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderItemService extends ServiceImpl<OrderItemMapper, OrderItem> implements IOrderItemService {

    @Override
    public List<OrderItem> listByOrderId(Long orderId) {
        return lambdaQuery()
                .eq(OrderItem::getOrderId, orderId)
                .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBatchItems(List<OrderItem> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            log.warn("订单明细列表为空，跳过保存");
            return true;
        }
        
        boolean success = saveBatch(orderItems);
        if (success) {
            log.info("批量保存订单明细成功，数量: {}", orderItems.size());
        } else {
            log.error("批量保存订单明细失败，数量: {}", orderItems.size());
        }
        
        return success;
    }
}