package com.cloudmall.framework.application.order.creator.validator.impl;

import com.cloudmall.framework.common.enums.BizErrorCode;
import com.cloudmall.framework.models.cart.ICartService;
import com.cloudmall.framework.models.cart.PurchaseMode;
import com.cloudmall.framework.application.order.context.TradeContext;
import com.cloudmall.framework.application.order.creator.validator.IOrderCreateValidator;
import com.cloudmall.framework.models.order.dto.TradeShopDTO;
import com.cloudmall.framework.models.order.dto.TradeShopItemDTO;
import com.cloudmall.framework.utils.AssertUtils;
import com.cloudmall.framework.context.AuthUserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CartGoodsValidator implements IOrderCreateValidator {

    private final ICartService cartService;

    @Autowired
    public CartGoodsValidator(ICartService cartService) {
        this.cartService = cartService;
    }

    @Override
    public int getOrder() {
        return 40;
    }

    @Override
    public boolean support(PurchaseMode mode) {
        return mode == PurchaseMode.CART_BUY;
    }

    @Override
    public void validate(TradeContext context) {
        Long userId = AuthUserContext.getUserId();
        for (TradeShopDTO shopDTO : context.getTradeDTO().getTradeItems()) {
            for (TradeShopItemDTO item : shopDTO.getTradeShopItemList()) {
                AssertUtils.isTrue(cartService.existsInCart(userId, item.getSkuId()), BizErrorCode.GOODS_NOT_IN_CART);
            }
        }
    }
}