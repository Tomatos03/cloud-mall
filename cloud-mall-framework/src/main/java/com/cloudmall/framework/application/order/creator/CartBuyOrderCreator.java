package com.cloudmall.framework.application.order.creator;

import java.util.List;

import com.cloudmall.framework.event.MQTag;
import com.cloudmall.framework.event.MQTopicProperties;
import com.cloudmall.framework.models.coupon.application.ICouponAppService;
import com.cloudmall.framework.event.TransactionCommitSendMQEvent;
import com.cloudmall.framework.event.cart.ClearCartEvent;
import com.cloudmall.framework.models.cart.PurchaseMode;
import com.cloudmall.framework.application.order.creator.validator.OrderCreateValidatorManager;
import com.cloudmall.framework.application.order.context.TradeContext;
import com.cloudmall.framework.models.order.dto.TradeShopDTO;
import com.cloudmall.framework.models.order.dto.TradeShopItemDTO;
import com.cloudmall.framework.models.order.service.IOrderItemService;
import com.cloudmall.framework.models.order.service.IOrderService;
import com.cloudmall.framework.utils.AuthUserUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 普通购物车下单创建器
 *
 * @author : Tomatos
 * @date : 2026/03/10
 */
@Component
public class CartBuyOrderCreator extends AbstractOrderCreator {
    public CartBuyOrderCreator(
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
        return PurchaseMode.CART_BUY;
    }

    @Override
    protected void afterCreateSuccess(TradeContext tradeContext) {
        super.afterCreateSuccess(tradeContext);
        pushCleanCartGoodsEvent(tradeContext.getTradeDTO()
                                            .getTradeItems());
    }

    private void pushCleanCartGoodsEvent(List<TradeShopDTO> tradeItems) {
        Long userId = AuthUserUtils.getUserId();
        applicationEventPublisher.publishEvent(
                new TransactionCommitSendMQEvent(
                        mqTopicProperties.getCart(),
                        MQTag.CART_CLEAR,
                        ClearCartEvent.builder()
                                      .userId(userId)
                                      .skuIds(tradeItems.stream()
                                                        .map(TradeShopDTO::getTradeShopItemList)
                                                        .flatMap(List::stream)
                                                        .map(TradeShopItemDTO::getSkuId)
                                                        .toList())
                                      .build()
                )
        );
    }
}
