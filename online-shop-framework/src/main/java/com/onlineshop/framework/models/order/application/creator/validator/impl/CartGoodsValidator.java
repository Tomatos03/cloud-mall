package com.onlineshop.framework.models.order.application.creator.validator.impl;

import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.models.cart.ICartService;
import com.onlineshop.framework.models.cart.PurchaseMode;
import com.onlineshop.framework.models.order.application.context.TradeContext;
import com.onlineshop.framework.models.order.application.creator.validator.IOrderCreateValidator;
import com.onlineshop.framework.models.order.dto.TradeShopDTO;
import com.onlineshop.framework.models.order.dto.TradeShopItemDTO;
import com.onlineshop.framework.utils.AssertUtils;
import com.onlineshop.framework.utils.AuthUserUtils;
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
        Long userId = AuthUserUtils.getUserId();
        for (TradeShopDTO shopDTO : context.getTradeDTO().getTradeItems()) {
            for (TradeShopItemDTO item : shopDTO.getTradeShopItemList()) {
                AssertUtils.isTrue(cartService.existsInCart(userId, item.getSkuId()), BizErrorCode.GOODS_NOT_IN_CART);
            }
        }
    }
}