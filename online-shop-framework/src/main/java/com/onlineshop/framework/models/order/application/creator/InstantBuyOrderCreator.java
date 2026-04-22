package com.onlineshop.framework.models.order.application.creator;

import com.onlineshop.framework.event.MQTopicProperties;
import com.onlineshop.framework.models.cart.PurchaseMode;
import com.onlineshop.framework.models.order.application.creator.validator.OrderCreateValidatorManager;
import com.onlineshop.framework.models.order.service.IOrderItemService;
import com.onlineshop.framework.models.order.service.IOrderService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 立即购买下单创建器
 *
 * @author : Tomatos
 * @date : 2026/03/10
 */
@Component
public class InstantBuyOrderCreator extends AbstractOrderCreator {

    public InstantBuyOrderCreator(
            IOrderService orderService,
            IOrderItemService orderItemService,
            ApplicationEventPublisher applicationEventPublisher,
            MQTopicProperties mqTopicProperties,
            OrderCreateValidatorManager validatorManager
    ) {
        super(
                orderService,
                orderItemService,
                applicationEventPublisher,
                mqTopicProperties,
                validatorManager
        );
    }

    @Override
    public PurchaseMode getSupportPurchaseMode() {
        return PurchaseMode.INSTANT_BUY;
    }
}
