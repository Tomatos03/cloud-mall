package com.onlineshop.framework.application.order.context;

import com.onlineshop.framework.models.address.Address;
import com.onlineshop.framework.models.cart.PurchaseMode;
import com.onlineshop.framework.models.coupon.application.vo.CouponCalcResult;
import com.onlineshop.framework.models.order.dto.OrderCreateResultDTO;
import com.onlineshop.framework.models.goods.sku.GoodsSku;
import com.onlineshop.framework.models.order.dto.TradeDTO;
import com.onlineshop.framework.models.order.entity.Order;
import com.onlineshop.framework.models.order.entity.OrderItem;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 下单流程上下文
 * 承载校验与构建阶段的中间数据，避免污染请求DTO。
 */
@Data
public class TradeContext {
    private final TradeDTO tradeDTO;
    private Address address;
    private Map<Long, GoodsSku> skuMap;
    private Order payOrder;
    private List<List<OrderItem>> shopOrderItems;
    private List<Order> orders;
    private List<OrderItem> orderItems;
    private Map<Long, CouponCalcResult> shopCouponResults;

    public OrderCreateResultDTO getCreateResult() {
        return OrderCreateResultDTO.builder()
                                   .orderNo(payOrder.getNo())
                                   .expireTime(payOrder.getCreateTime().plusMinutes(30))
                                   .build();
    }
}
