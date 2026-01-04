package com.onlineshop.framework.models.order.strategy;

import com.onlineshop.framework.models.order.dto.TradeDTO;
import com.onlineshop.framework.models.cart.CartType;

/**
 * 订单校验策略接口
 *
 * @author : Tomatos
 * @date : 2025/12/24
 */
public interface OrderValidateStrategy {
    
    /**
     * 校验订单数据
     *
     * @param tradeDTO 交易数据
     */
    void validate(TradeDTO tradeDTO);
    
    /**
     * 获取支持的购物车类型
     *
     * @return 购物车类型
     */
    CartType supportCartType();
}