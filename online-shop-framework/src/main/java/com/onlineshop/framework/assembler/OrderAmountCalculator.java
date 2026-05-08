package com.onlineshop.framework.assembler;

import com.onlineshop.framework.models.order.vo.StoreOrderItemVO;
import com.onlineshop.framework.models.order.vo.StoreOrderVO;
import com.onlineshop.framework.utils.money.Money;
import com.onlineshop.framework.utils.money.MoneyUtil;

import java.util.List;

/**
 * 订单金额与数量计算器
 */
public final class OrderAmountCalculator {

    private OrderAmountCalculator() {
    }

    /**
     * 计算商品明细总价
     */
    public static String calculateItemsTotalPrice(List<StoreOrderItemVO> items) {
        List<Money> monies = items.stream()
                                  .map(item -> Money.ofYuan(item.getTotalPrice()))
                                  .toList();
        return MoneyUtil.sum(monies).toYuanString();
    }

    /**
     * 计算商品明细总数量
     */
    public static Long calculateItemsCount(List<StoreOrderItemVO> items) {
        return items.stream().mapToLong(StoreOrderItemVO::getQuantity).sum();
    }

    /**
     * 计算店铺订单总价
     */
    public static String calculateStoreOrdersTotalPrice(List<StoreOrderVO> storeOrders) {
        List<Money> monies = storeOrders.stream()
                                        .map(order -> Money.ofYuan(order.getTotalPrice()))
                                        .toList();
        return MoneyUtil.sum(monies).toYuanString();
    }

    /**
     * 计算店铺订单总数量
     */
    public static Long calculateStoreOrdersCount(List<StoreOrderVO> storeOrders) {
        return storeOrders.stream().mapToLong(StoreOrderVO::getCount).sum();
    }
}
