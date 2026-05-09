package com.cloudmall.framework.assembler;

import com.cloudmall.framework.models.order.entity.Order;
import com.cloudmall.framework.models.order.vo.StoreOrderItemVO;
import com.cloudmall.framework.models.order.vo.StoreOrderVO;
import com.cloudmall.framework.utils.money.Money;

import java.util.List;

/**
 * StoreOrderVO 组装器
 */
public final class StoreOrderVOAssembler {

    private StoreOrderVOAssembler() {
    }

    /**
     * 组装 StoreOrderVO
     */
    public static StoreOrderVO assemble(List<StoreOrderItemVO> items, Order order, String storeName) {
        return StoreOrderVO.builder()
                           .orderNo(order.getNo())
                           .storeId(order.getStoreId())
                           .storeName(storeName)
                           .status(order.getStatus())
                           .items(items)
                           .totalPrice(OrderAmountCalculator.calculateItemsTotalPrice(items))
                           .couponDiscount(order.getCouponDiscount() != null ? Money.ofCents(order.getCouponDiscount()).toYuanString() : null)
                           .payAmount(order.getPayAmount() != null ? Money.ofCents(order.getPayAmount()).toYuanString() : null)
                           .count(OrderAmountCalculator.calculateItemsCount(items))
                           .build();
    }
}
