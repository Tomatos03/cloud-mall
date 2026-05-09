package com.cloudmall.framework.application.order.creator;

import com.cloudmall.framework.event.MQTopicProperties;
import com.cloudmall.framework.models.cart.PurchaseMode;
import com.cloudmall.framework.models.coupon.application.ICouponAppService;
import com.cloudmall.framework.application.order.creator.validator.OrderCreateValidatorManager;
import com.cloudmall.framework.models.order.service.IOrderItemService;
import com.cloudmall.framework.models.order.service.IOrderService;
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
            OrderCreateValidatorManager validatorManager,
            ICouponAppService couponAppService
    ) {
        super(
                orderService,
                orderItemService,
                applicationEventPublisher,
                mqTopicProperties,
                validatorManager,
                couponAppService
        );
    }

    @Override
    public PurchaseMode getSupportPurchaseMode() {
        return PurchaseMode.INSTANT_BUY;
    }
}
