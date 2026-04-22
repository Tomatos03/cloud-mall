package com.onlineshop.framework.models.order.application.creator.validator;

import com.onlineshop.framework.models.cart.PurchaseMode;
import com.onlineshop.framework.models.order.application.context.TradeContext;

/**
 * 订单创建校验器接口
 */
public interface IOrderCreateValidator {

    /**
     * 执行校验，校验不通过直接抛业务异常
     */
    void validate(TradeContext context);

    /**
     * 优先级，数字越小越先执行
     */
    default int getOrder() {
        return 0;
    }

    default boolean support(PurchaseMode mode) {
        return true;
    }
}
