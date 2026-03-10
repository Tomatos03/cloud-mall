package com.onlineshop.framework.models.order.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.exception.BizException;
import com.onlineshop.framework.models.order.entity.Order;
import com.onlineshop.framework.models.order.enums.OrderStatus;
import com.onlineshop.framework.models.order.enums.OrderType;
import com.onlineshop.framework.models.order.enums.ParentOrderStatus;
import com.onlineshop.framework.models.order.mapper.OrderMapper;
import com.onlineshop.framework.models.order.service.IOrderService;
import com.onlineshop.framework.models.order.state.OrderStatusMachine;
import com.onlineshop.framework.utils.AssertUtils;
import com.onlineshop.framework.utils.AuthUserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单领域服务实现
 * 仅负责 order 表操作与订单状态流转
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService extends ServiceImpl<OrderMapper, Order> implements IOrderService {
    @Override
    public Order queryByOrderNo(String orderNo) {
        return lambdaQuery().eq(Order::getNo, orderNo).one();
    }

    @Override
    public Order queryUserOrderByOrderNo(String orderNo) {
        return lambdaQuery().eq(Order::getNo, orderNo)
                            .eq(Order::getUserId, AuthUserUtils.getUserId())
                            .one();
    }

    @Override
    public Order queryByOrderNoAndStoreIds(String orderNo, List<Long> storeIds) {
        if (CollUtil.isEmpty(storeIds)) {
            return null;
        }

        return lambdaQuery().eq(Order::getNo, orderNo)
                            .in(Order::getStoreId, storeIds)
                            .one();
    }

    @Override
    public List<Order> querySubOrders(Long parentId) {
        return lambdaQuery().eq(Order::getParentId, parentId).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateOrderStatus(Order order, OrderStatus newStatus) {
        AssertUtils.notNull(order, BizErrorCode.ORDER_NOT_EXIST);

        String oldStatus = order.getStatus();
        if (!OrderStatusMachine.validateTransition(OrderStatus.of(oldStatus), newStatus)) {
            return false;
        }

        order.setStatus(newStatus.getCode());
        syncUpdateParentOrSubOrderStatus(order);

        return lambdaUpdate().eq(Order::getStatus, oldStatus)
                             .eq(Order::getId, order.getId())
                             .set(Order::getStatus, newStatus.getCode())
                             .set(StrUtil.isNotBlank(order.getReason()), Order::getReason, order.getReason())
                             .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void savePayOrder(Order payOrder) {
        if (!OrderType.PARENT.getCode().equals(payOrder.getOrderType())) {
            return;
        }

        if (!save(payOrder)) {
            log.error("支付订单保存失败");
            throw new BizException(BizErrorCode.ORDER_CREATE_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrders(List<Order> orders) {
        if (CollUtil.isEmpty(orders)) {
            return;
        }

        if (!saveBatch(orders)) {
            log.error("订单保存失败");
            throw new BizException(BizErrorCode.ORDER_CREATE_FAILED);
        }
    }

    @Override
    public List<Order> queryShippedOrders() {
        return lambdaQuery().eq(Order::getStatus, OrderStatus.SHIPPED.getCode()).list();
    }

    @Override
    public List<Order> queryTimeoutCreatedOrders(LocalDateTime deadline) {
        return lambdaQuery().eq(Order::getStatus, OrderStatus.CREATED.getCode())
                            .le(Order::getCreateTime, deadline)
                            .list();
    }

    private void syncUpdateParentOrSubOrderStatus(Order order) {
        switch (OrderType.of(order.getOrderType())) {
            case PARENT -> syncSubOrderStatus(order);
            case SUB -> syncParentOrderStatus(order);
            default -> {
            }
        }
    }

    private void syncSubOrderStatus(Order order) {
        List<Order> subOrders = querySubOrders(order.getId());
        if (CollUtil.isEmpty(subOrders)) {
            return;
        }

        for (Order subOrder : subOrders) {
            subOrder.setStatus(order.getStatus());
        }
        updateBatchById(subOrders);
    }

    private void syncParentOrderStatus(Order order) {
        Order parentOrder = getById(order.getParentId());
        if (parentOrder == null) {
            return;
        }

        boolean allSameStatus = querySubOrders(parentOrder.getId()).stream()
                                                                    .allMatch(o -> o.getStatus().equals(order.getStatus()));
        parentOrder.setStatus(allSameStatus ? order.getStatus() : ParentOrderStatus.PROCESSING.getCode());
        updateById(parentOrder);
    }
}
