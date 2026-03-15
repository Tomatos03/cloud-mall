package com.onlineshop.framework.models.order.application.creator;

import java.util.List;

import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.event.MQTag;
import com.onlineshop.framework.event.MQTopicProperties;
import com.onlineshop.framework.event.TransactionCommitSendMQEvent;
import com.onlineshop.framework.event.cart.ClearCartEvent;
import com.onlineshop.framework.models.address.IAddressService;
import com.onlineshop.framework.models.cart.ICartService;
import com.onlineshop.framework.models.cart.PurchaseMode;
import com.onlineshop.framework.models.goods.sku.IGoodsSkuService;
import com.onlineshop.framework.models.goods.spu.IGoodsService;
import com.onlineshop.framework.models.order.dto.TradeDTO;
import com.onlineshop.framework.models.order.dto.TradeShopDTO;
import com.onlineshop.framework.models.order.dto.TradeShopItemDTO;
import com.onlineshop.framework.models.order.service.IOrderItemService;
import com.onlineshop.framework.models.order.service.IOrderService;
import com.onlineshop.framework.models.store.IStoreService;
import com.onlineshop.framework.utils.AssertUtils;
import com.onlineshop.framework.utils.AuthUserUtils;
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
    private final ICartService cartService;

    public CartBuyOrderCreator(
            IOrderService orderService,
            IOrderItemService orderItemService,
            IAddressService addressService,
            IGoodsService goodsService,
            IGoodsSkuService goodsSkuService,
            IStoreService storeService,
            ICartService cartService,
            ApplicationEventPublisher applicationEventPublisher,
            MQTopicProperties mqTopicProperties
    ) {
        super(
                orderService,
                orderItemService,
                addressService,
                goodsService,
                goodsSkuService,
                storeService,
                applicationEventPublisher,
                mqTopicProperties
        );
        this.cartService = cartService;
    }

    @Override
    public PurchaseMode getSupportPurchaseMode() {
        return PurchaseMode.CART_BUY;
    }

    @Override
    protected void validateFillAdditional(TradeContext tradeContext) {
        TradeDTO tradeDTO = tradeContext.getTradeDTO();
        Long userId = AuthUserUtils.getUserId();

        for (TradeShopDTO shopDTO : tradeDTO.getTradeItems()) {
            for (TradeShopItemDTO item : shopDTO.getTradeShopItemList()) {
                AssertUtils.isTrue(cartService.existsInCart(userId, item.getSkuId()), BizErrorCode.GOODS_NOT_IN_CART);
            }
        }
    }

    @Override
    protected void afterCreateSuccess(TradeContext tradeContext) {
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
