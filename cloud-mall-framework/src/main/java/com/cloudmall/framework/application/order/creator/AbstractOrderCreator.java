package com.cloudmall.framework.application.order.creator;

import cn.hutool.core.collection.CollUtil;
import com.cloudmall.framework.models.coupon.application.ICouponAppService;
import com.cloudmall.framework.models.coupon.application.vo.CouponCalcResult;
import com.cloudmall.framework.event.MQTag;
import com.cloudmall.framework.event.MQTopicProperties;
import com.cloudmall.framework.event.TransactionCommitSendMQEvent;
import com.cloudmall.framework.models.address.Address;
import com.cloudmall.framework.models.cart.PurchaseMode;
import com.cloudmall.framework.models.goods.sku.GoodsSku;
import com.cloudmall.framework.application.order.context.TradeContext;
import com.cloudmall.framework.application.order.creator.validator.OrderCreateValidatorManager;
import com.cloudmall.framework.models.order.dto.OrderCreateResultDTO;
import com.cloudmall.framework.models.order.dto.TradeDTO;
import com.cloudmall.framework.models.order.dto.TradeShopDTO;
import com.cloudmall.framework.models.order.dto.TradeShopItemDTO;
import com.cloudmall.framework.models.order.entity.Order;
import com.cloudmall.framework.models.order.entity.OrderItem;
import com.cloudmall.framework.models.order.enums.OrderStatus;
import com.cloudmall.framework.models.order.enums.OrderType;
import com.cloudmall.framework.models.order.service.IOrderItemService;
import com.cloudmall.framework.models.order.service.IOrderService;
import com.cloudmall.framework.utils.AuthUserUtils;
import com.cloudmall.framework.utils.IDNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 订单创建器抽象基类
 * 使用模板方法统一下单流程，子类只需扩展差异化行为
 *
 * @author : Tomatos
 * @date : 2026/03/10
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractOrderCreator implements IOrderCreator {
    protected final IOrderService orderService;
    protected final IOrderItemService orderItemService;
    protected final ApplicationEventPublisher applicationEventPublisher;
    protected final MQTopicProperties mqTopicProperties;
    protected final OrderCreateValidatorManager validatorManager;
    protected final ICouponAppService couponAppService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderCreateResultDTO create(TradeDTO tradeDTO) {
        TradeContext tradeContext = new TradeContext(tradeDTO);
        validatorManager.validate(tradeContext, getSupportPurchaseMode());

        buildTrade(tradeContext);
        persistTrade(tradeContext);
        afterCreateSuccess(tradeContext);
        OrderCreateResultDTO result = tradeContext.getCreateResult();

        log.info("订单创建成功, cartType: {}, orderNo: {}", getSupportPurchaseMode(), result.getOrderNo());
        return result;
    }

    /**
     * 子类指定下单类型
     */
    public abstract PurchaseMode getSupportPurchaseMode();

    /**
     * 子类可覆盖返回不同订单初始状态
     */
    protected String getOrderStatus() {
        return OrderStatus.CREATED.getCode();
    }

    /**
     * 创建成功后的扩展点
     */
    protected void afterCreateSuccess(TradeContext tradeContext) {
        lockCoupons(tradeContext);
        pushOrderTimeoutCancelEvent(tradeContext.getPayOrder()
                                                .getNo());
    }

    private void lockCoupons(TradeContext tradeContext) {
        if (tradeContext.getShopCouponResults() == null) {
            return;
        }
        String orderNo = tradeContext.getPayOrder().getNo();
        for (CouponCalcResult result : tradeContext.getShopCouponResults().values()) {
            if (result.getUserCouponId() != null) {
                couponAppService.lockCoupon(result.getUserCouponId(), orderNo);
            }
        }
    }


    /**
     * 构建阶段：按 orderItem -> order -> parentOrder 的顺序在内存中完成组装。
     */
    private void buildTrade(TradeContext tradeContext) {
        buildShopOrderItems(tradeContext);
        buildShopOrders(tradeContext);
        determinePayOrder(tradeContext);
    }

    /**
     * 持久化阶段：按 parentOrder -> order -> orderItem 的顺序落库并回填关联关系。
     */
    private void persistTrade(TradeContext tradeContext) {
        orderService.save(tradeContext.getPayOrder());
        fillParentOrderToOrders(tradeContext);
        orderService.saveOrders(tradeContext.getOrders());
        fillOrderItems(tradeContext);
        orderItemService.saveBatchItems(tradeContext.getOrderItems());
    }

    private void buildShopOrderItems(TradeContext tradeContext) {
        TradeDTO tradeDTO = tradeContext.getTradeDTO();
        List<TradeShopDTO> shopList = tradeDTO.getTradeItems();
        List<List<OrderItem>> shopOrderItems = new ArrayList<>(shopList.size());

        for (TradeShopDTO shopDTO : shopList) {
            List<OrderItem> orderItems = new ArrayList<>(shopDTO.getTradeShopItemList()
                                                                .size());
            CouponCalcResult couponResult = tradeContext.getShopCouponResults() != null
                    ? tradeContext.getShopCouponResults().get(shopDTO.getStoreId()) : null;
            for (TradeShopItemDTO itemDTO : shopDTO.getTradeShopItemList()) {
                GoodsSku sku = tradeContext.getSkuMap()
                                           .get(itemDTO.getSkuId());
                long itemTotalPrice = sku.getPrice() * itemDTO.getQuantity();
                long itemDiscount = 0;
                if (couponResult != null && couponResult.getItemDiscounts() != null) {
                    itemDiscount = couponResult.getItemDiscounts().getOrDefault(itemDTO.getSkuId(), 0L);
                }
                orderItems.add(buildOrderItem(sku, itemDTO.getQuantity(), itemTotalPrice, itemDiscount));
            }
            shopOrderItems.add(orderItems);
        }
        tradeContext.setShopOrderItems(shopOrderItems);
    }

    private void buildShopOrders(TradeContext tradeContext) {
        List<TradeShopDTO> shopList = tradeContext.getTradeDTO()
                                                  .getTradeItems();
        List<List<OrderItem>> shopOrderItems = tradeContext.getShopOrderItems();
        List<Order> orders = new ArrayList<>(shopList.size());
        for (int i = 0; i < shopList.size(); i++) {
            TradeShopDTO shopDTO = shopList.get(i);
            List<OrderItem> orderItems = shopOrderItems.get(i);
            long orderTotalPrice = calOrderItemsTotalPrice(orderItems);
            CouponCalcResult couponResult = tradeContext.getShopCouponResults() != null
                    ? tradeContext.getShopCouponResults().get(shopDTO.getStoreId()) : null;
            long couponDiscount = couponResult != null ? couponResult.getTotalDiscount() : 0;
            long payAmount = orderTotalPrice - couponDiscount;

            Order order = buildOrder(
                    shopDTO.getStoreId(),
                    orderItems.size(),
                    orderTotalPrice,
                    tradeContext.getAddress()
            );
            order.setCouponId(couponResult != null ? couponResult.getUserCouponId() : null);
            order.setCouponDiscount(couponDiscount);
            order.setPayAmount(payAmount);
            orders.add(order);
        }
        tradeContext.setOrders(orders);
    }

    private static long calOrderItemsTotalPrice(List<OrderItem> orderItems) {
        return orderItems.stream()
                         .mapToLong(OrderItem::getTotalPrice)
                         .sum();
    }

    private void determinePayOrder(TradeContext tradeContext) {
        tradeContext.setPayOrder(createPayOrder(tradeContext));
        log.info("订单构建成功, userId: {}, 订单类型: {}", AuthUserUtils.getUserId(), getSupportPurchaseMode());
    }

    private Order createPayOrder(TradeContext tradeContext) {
        List<Order> orders = tradeContext.getOrders();
        if (orders.size() > 1) {
            orders.forEach(order -> order.setOrderType(OrderType.SUB.getCode()));
            return createParentOrder(orders);
        }
        Order normalOrder = orders.remove(0);
        normalOrder.setOrderType(OrderType.NORMAL.getCode());
        return normalOrder;
    }

    private OrderItem buildOrderItem(
            GoodsSku sku,
            int quantity,
            long itemTotalPrice,
            long itemDiscount
    ) {
        return OrderItem.builder()
                        .skuId(sku.getId())
                        .goodsId(sku.getGoodsId())
                        .goodsName(sku.getGoodsName())
                        .goodsMainImageUrl(sku.getMainImageUrl())
                        .goodsPrice(sku.getPrice())
                        .quantity(quantity)
                        .originalPrice(itemTotalPrice)
                        .discountAmount(itemDiscount)
                        .totalPrice(itemTotalPrice - itemDiscount)
                        .skuSpecs(sku.getSpecSnapshot())
                        .build();
    }

    private Order createParentOrder(List<Order> subOrders) {
        long totalPrice = subOrders.stream()
                                   .mapToLong(Order::getTotalPrice)
                                   .sum();
        long couponDiscount = subOrders.stream()
                                       .mapToLong(o -> o.getCouponDiscount() != null ? o.getCouponDiscount() : 0)
                                       .sum();
        long payAmount = subOrders.stream()
                                  .mapToLong(o -> o.getPayAmount() != null ? o.getPayAmount() : o.getTotalPrice())
                                  .sum();

        Order.OrderBuilder orderBuilder = Order.builder()
                                               .no(IDNumber.generateOrderNo())
                                               .userId(AuthUserUtils.getUserId())
                                               .totalPrice(totalPrice)
                                               .couponDiscount(couponDiscount)
                                               .payAmount(payAmount)
                                               .quantity(subOrders.size())
                                               .status(OrderStatus.CREATED.getCode())
                                               .orderType(OrderType.PARENT.getCode());

        if (CollUtil.isNotEmpty(subOrders)) {
            Order firstSubOrder = subOrders.get(0);
            orderBuilder.userName(firstSubOrder.getUserName())
                        .phone(firstSubOrder.getPhone())
                        .address(firstSubOrder.getAddress());
        }
        return orderBuilder.build();
    }

    private Order buildOrder(long storeId, int quantity, long orderTotalPrice, Address address) {
        return Order.builder()
                    .no(IDNumber.generateOrderNo())
                    .userId(AuthUserUtils.getUserId())
                    .storeId(storeId)
                    .quantity(quantity)
                    .totalPrice(orderTotalPrice)
                    .createTime(LocalDateTime.now())
                    .status(getOrderStatus())
                    .userName(address.getReceiver())
                    .phone(address.getPhone())
                    .address(address.getFullAddress() + "/" + address.getDetail())
                    .build();
    }

    private void fillParentOrderToOrders(TradeContext tradeContext) {
        Order payOrder = tradeContext.getPayOrder();
        if (!OrderType.PARENT.getCode()
                             .equals(payOrder.getOrderType())) {
            return;
        }

        for (Order order : tradeContext.getOrders()) {
            order.setParentId(payOrder.getId());
        }
    }

    private void fillOrderItems(TradeContext tradeContext) {
        List<Order> orders = tradeContext.getOrders();
        List<List<OrderItem>> shopOrderItems = tradeContext.getShopOrderItems();
        List<OrderItem> orderItems = new ArrayList<>();
        if (CollUtil.isEmpty(orders)) {
            List<OrderItem> items = shopOrderItems.get(0);
            supplementOrderItemOrderId(tradeContext.getPayOrder(), items);
            orderItems.addAll(items);
            tradeContext.setOrderItems(orderItems);
            return;
        }
        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            List<OrderItem> items = shopOrderItems.get(i);
            supplementOrderItemOrderId(order, items);
            orderItems.addAll(items);
        }
        tradeContext.setOrderItems(orderItems);
    }

    private void pushOrderTimeoutCancelEvent(String orderNo) {
        Message<String> message = MessageBuilder.withPayload(orderNo)
                                                .setHeader(RocketMQHeaders.KEYS, orderNo)
                                                .build();

        applicationEventPublisher.publishEvent(
                new TransactionCommitSendMQEvent(
                        mqTopicProperties.getOrder(),
                        MQTag.ORDER_TIMEOUT_CANCEL,
                        message,
                        18
                )
        );
    }

    private void supplementOrderItemOrderId(Order order, List<OrderItem> orderItems) {
        for (OrderItem orderItem : orderItems) {
            orderItem.setOrderId(order.getId());
        }
    }
}
