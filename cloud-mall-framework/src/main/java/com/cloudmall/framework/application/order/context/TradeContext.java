package com.cloudmall.framework.application.order.context;

import com.cloudmall.framework.models.address.Address;
import com.cloudmall.framework.models.cart.PurchaseMode;
import com.cloudmall.framework.models.coupon.application.vo.CouponCalcResult;
import com.cloudmall.framework.models.order.dto.OrderCreateResultDTO;
import com.cloudmall.framework.models.goods.sku.GoodsSku;
import com.cloudmall.framework.models.order.dto.TradeDTO;
import com.cloudmall.framework.models.order.entity.Order;
import com.cloudmall.framework.models.order.entity.OrderItem;
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
