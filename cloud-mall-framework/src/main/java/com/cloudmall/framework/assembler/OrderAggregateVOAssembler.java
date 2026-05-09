package com.cloudmall.framework.assembler;

import com.cloudmall.framework.models.order.entity.Order;
import com.cloudmall.framework.models.order.vo.OrderAggregateVO;
import com.cloudmall.framework.models.order.vo.StoreOrderVO;

import java.util.List;

/**
 * OrderAggregateVO 组装器
 */
public final class OrderAggregateVOAssembler {

    private OrderAggregateVOAssembler() {
    }

    /**
     * 组装 OrderAggregateVO
     */
    public static OrderAggregateVO assemble(Order topOrder, List<StoreOrderVO> storeOrders) {
        return OrderAggregateVO.builder()
                               .orderNo(topOrder.getNo())
                               .status(topOrder.getStatus())
                               .createTime(topOrder.getCreateTime())
                               .expireTime(topOrder.getCreateTime().plusMinutes(30L))
                               .reason(topOrder.getReason())
                               .storeOrders(storeOrders)
                               .totalPrice(OrderAmountCalculator.calculateStoreOrdersTotalPrice(storeOrders))
                               .count(OrderAmountCalculator.calculateStoreOrdersCount(storeOrders))
                               .build();
    }
}
