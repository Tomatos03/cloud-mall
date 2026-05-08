package com.onlineshop.framework.application.order.creator;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.models.cart.PurchaseMode;
import com.onlineshop.framework.utils.AssertUtils;

/**
 * 订单创建器工厂
 * 根据购物车类型返回对应的创建器
 *
 * @author : Tomatos
 * @date : 2026/03/10
 */
@Component
@RequiredArgsConstructor
public class OrderCreatorFactory {

    private final List<IOrderCreator> orderCreators;
    private final Map<PurchaseMode, IOrderCreator> creatorMap = new EnumMap<>(PurchaseMode.class);

    @PostConstruct
    public void initCreatorMap() {
        for (IOrderCreator orderCreator : orderCreators) {
            creatorMap.put(orderCreator.getSupportPurchaseMode(), orderCreator);
        }
    }

    public IOrderCreator getOrderCreator(PurchaseMode purchaseMode) {
        AssertUtils.notNull(purchaseMode, BizErrorCode.UNKNOW_CART_TYPE);
        IOrderCreator orderCreator = creatorMap.get(purchaseMode);
        AssertUtils.notNull(orderCreator, BizErrorCode.UNKNOW_CART_TYPE);
        return orderCreator;
    }
}
