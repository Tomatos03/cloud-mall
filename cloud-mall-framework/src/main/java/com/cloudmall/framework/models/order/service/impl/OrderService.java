package com.cloudmall.framework.models.order.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudmall.framework.common.enums.BizErrorCode;
import com.cloudmall.framework.exception.BizException;
import com.cloudmall.framework.models.order.dto.OrderParamsDTO;
import com.cloudmall.framework.models.order.entity.Order;
import com.cloudmall.framework.models.order.enums.OrderStatus;
import com.cloudmall.framework.models.order.enums.OrderType;
import com.cloudmall.framework.models.order.enums.ParentOrderStatus;
import com.cloudmall.framework.models.order.mapper.OrderMapper;
import com.cloudmall.framework.models.order.service.IOrderService;
import com.cloudmall.framework.models.order.wrapper.OrderQueryWrapper;
import com.cloudmall.framework.utils.AssertUtils;
import com.cloudmall.framework.context.AuthUserContext;
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
        return lambdaQuery().eq(Order::getNo, orderNo)
                            .one();
    }

    @Override
    public Order queryMyOrder(String orderNo) {
        return lambdaQuery().eq(Order::getNo, orderNo)
                            .eq(Order::getUserId, AuthUserContext.getUserId())
                            .one();
    }

    @Override
    public Order queryStoreOrder(String orderNo, Long storeId) {
        AssertUtils.notNull(storeId, BizErrorCode.STORE_NOT_EXIST);
        return lambdaQuery().eq(Order::getNo, orderNo)
                            .eq(Order::getStoreId, storeId)
                            .one();
    }

    @Override
    public Order queryCancelableOrder(String orderNo) {
        if (AuthUserContext.isMerchantAccount()) {
            return queryStoreOrder(orderNo, AuthUserContext.getStoreId());
        }
        return queryMyOrder(orderNo);
    }

    @Override
    public List<Order> querySubOrders(Long parentId) {
        return lambdaQuery().eq(Order::getParentId, parentId)
                            .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderStatus(String orderNo, OrderStatus newStatus) {
        doUpdateOrderStatus(queryByOrderNo(orderNo), newStatus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderStatus(Order order, OrderStatus newStatus) {
        doUpdateOrderStatus(order, newStatus);
    }

    private void doUpdateOrderStatus(Order order, OrderStatus newStatus) {
        AssertUtils.notNull(order, BizErrorCode.ORDER_NOT_EXIST);

        String oldStatus = order.getStatus();
        OrderStatus from = OrderStatus.of(oldStatus);
        AssertUtils.isTrue(from.canTransferTo(newStatus), BizErrorCode.INVALID_ORDER_STATUS);

        order.setStatus(newStatus.getCode());
        syncUpdateParentOrSubOrderStatus(order);

        lambdaUpdate().eq(Order::getStatus, oldStatus)
                      .eq(Order::getId, order.getId())
                      .set(Order::getStatus, newStatus.getCode())
                      .set(StrUtil.isNotBlank(order.getReason()), Order::getReason, order.getReason())
                      .update();
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
        return lambdaQuery().eq(Order::getStatus, OrderStatus.SHIPPED.getCode())
                            .list();
    }

    @Override
    public List<Order> queryTimeoutOrders(LocalDateTime deadline) {
        return lambdaQuery().eq(Order::getStatus, OrderStatus.CREATED.getCode())
                            .le(Order::getCreateTime, deadline)
                            .list();
    }

    @Override
    public IPage<Order> pageQueryOrders(OrderParamsDTO queryDTO) {
        Page<Order> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        LambdaQueryWrapper<Order> wrapper = OrderQueryWrapper.build(queryDTO);
        return page(page, wrapper);
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
                                                                   .allMatch(o -> o.getStatus()
                                                                                   .equals(order.getStatus()));
        parentOrder.setStatus(allSameStatus ? order.getStatus() : ParentOrderStatus.PROCESSING.getCode());
        updateById(parentOrder);
    }
}
