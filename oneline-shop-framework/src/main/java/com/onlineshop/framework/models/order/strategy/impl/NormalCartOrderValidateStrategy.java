package com.onlineshop.framework.models.order.strategy.impl;

import com.onlineshop.framework.models.order.dto.TradeDTO;
import com.onlineshop.framework.models.order.dto.TradeShopDTO;
import com.onlineshop.framework.models.order.dto.TradeShopItemDTO;
import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.models.cart.CartType;
import com.onlineshop.framework.exception.BusinessException;
import com.onlineshop.framework.models.cart.ICartService;
import com.onlineshop.framework.models.goods.spu.IGoodsService;
import com.onlineshop.framework.models.goods.sku.IGoodsSkuService;
import com.onlineshop.framework.utils.context.UserContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 普通购物车订单校验策略
 * 职责：在订单创建前完成所有必要的数据校验
 * 
 * 多规格商品支持更新（2025-01-02）:
 * - 从商品级别验证 → SKU级别验证
 * - 购物车中检查SKU而非商品
 *
 * @author : Tomatos
 * @date : 2025/12/24
 */
@Slf4j
@Component
public class NormalCartOrderValidateStrategy extends AbstractOrderValidateStrategy {

    private final ICartService cartService;

    public NormalCartOrderValidateStrategy(
            IGoodsService goodsService,
            IGoodsSkuService goodsSkuService,
            ICartService cartService) {
        super(goodsService, goodsSkuService);
        this.cartService = cartService;
    }

    /**
     * 购物车特定校验：校验SKU是否在购物车中
     */
    @Override
    protected void doAdditionalValidate(TradeDTO tradeDTO) {
        Long userId = UserContextHolder.getUserId();
        
        for (TradeShopDTO shopDTO : tradeDTO.getTradeItems()) {
            for (TradeShopItemDTO item : shopDTO.getTradeShopItemList()) {
                validateCartItem(userId, item.getSkuId());
            }
        }
    }

    /**
     * 校验购物车中是否存在该SKU
     *
     * @param userId 用户ID
     * @param skuId  SKU ID
     */
    private void validateCartItem(Long userId, Long skuId) {
        boolean existsInCart = cartService.existsInCart(userId, skuId);
        if (!existsInCart) {
            log.error("购物车中不存在该SKU, userId: {}, skuId: {}", userId, skuId);
            throw new BusinessException(BizErrorCode.GOODS_NOT_IN_CART);
        }
    }

    @Override
    public CartType getSupportCartType() {
        return CartType.NORMAL;
    }
}