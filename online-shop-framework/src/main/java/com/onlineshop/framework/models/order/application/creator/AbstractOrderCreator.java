package com.onlineshop.framework.models.order.application.creator;

import cn.hutool.core.collection.CollUtil;
import com.onlineshop.framework.event.MQTag;
import com.onlineshop.framework.event.MQTopicProperties;
import com.onlineshop.framework.event.TransactionCommitSendMQEvent;
import com.onlineshop.framework.models.address.Address;
import com.onlineshop.framework.models.cart.PurchaseMode;
import com.onlineshop.framework.models.goods.sku.GoodsSku;
import com.onlineshop.framework.models.order.application.context.TradeContext;
import com.onlineshop.framework.models.order.application.creator.validator.OrderCreateValidatorManager;
import com.onlineshop.framework.models.order.dto.OrderCreateResultDTO;
import com.onlineshop.framework.models.order.dto.TradeDTO;
import com.onlineshop.framework.models.order.dto.TradeShopDTO;
import com.onlineshop.framework.models.order.dto.TradeShopItemDTO;
import com.onlineshop.framework.models.order.entity.Order;
import com.onlineshop.framework.models.order.entity.OrderItem;
import com.onlineshop.framework.models.order.enums.OrderStatus;
import com.onlineshop.framework.models.order.enums.OrderType;
import com.onlineshop.framework.models.order.service.IOrderItemService;
import com.onlineshop.framework.models.order.service.IOrderService;
import com.onlineshop.framework.utils.AuthUserUtils;
import com.onlineshop.framework.utils.IDNumber;
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
        pushOrderTimeoutCancelEvent(tradeContext.getPayOrder()
                                                .getNo());
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
            for (TradeShopItemDTO itemDTO : shopDTO.getTradeShopItemList()) {
                GoodsSku sku = tradeContext.getSkuMap()
                                           .get(itemDTO.getSkuId());
                long itemTotalPrice = sku.getPrice() * itemDTO.getQuantity();
                orderItems.add(buildOrderItem(sku, itemDTO.getQuantity(), itemTotalPrice));
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
            Order order = buildOrder(
                    shopDTO.getStoreId(),
                    orderItems.size(),
                    calOrderItemsTotalPrice(orderItems),
                    tradeContext.getAddress()
            );
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
            long totalPrice
    ) {
        return OrderItem.builder()
                        .skuId(sku.getId())
                        .goodsId(sku.getGoodsId())
                        .goodsName(sku.getGoodsName())
                        .goodsMainImageUrl(sku.getMainImageUrl())
                        .goodsPrice(sku.getPrice())
                        .quantity(quantity)
                        .totalPrice(totalPrice)
                        .skuSpecs(sku.getSpecSnapshot())
                        .build();
    }

    private Order createParentOrder(List<Order> subOrders) {
        long totalPrice = subOrders.stream()
                                   .mapToLong(Order::getTotalPrice)
                                   .sum();

        Order.OrderBuilder orderBuilder = Order.builder()
                                               .no(IDNumber.generateOrderNo())
                                               .userId(AuthUserUtils.getUserId())
                                               .totalPrice(totalPrice)
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
