package com.onlineshop.framework.models.order.application;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlineshop.framework.models.order.strategy.OrderBuildStrategy;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.event.cart.ClearCartEvent;
import com.onlineshop.framework.event.order.OrderTimeoutCancelEvent;
import com.onlineshop.framework.models.address.Address;
import com.onlineshop.framework.models.address.IAddressService;
import com.onlineshop.framework.models.cart.CartType;
import com.onlineshop.framework.models.goods.sku.IGoodsSkuService;
import com.onlineshop.framework.models.goods.spu.IGoodsService;
import com.onlineshop.framework.models.order.dto.OrderCancelDTO;
import com.onlineshop.framework.models.order.dto.OrderCreateResultDTO;
import com.onlineshop.framework.models.order.dto.OrderParamsDTO;
import com.onlineshop.framework.models.order.dto.TradeDTO;
import com.onlineshop.framework.models.order.dto.TradeShopDTO;
import com.onlineshop.framework.models.order.dto.TradeShopItemDTO;
import com.onlineshop.framework.models.order.entity.Order;
import com.onlineshop.framework.models.order.entity.OrderItem;
import com.onlineshop.framework.models.order.enums.OrderStatus;
import com.onlineshop.framework.models.order.enums.OrderType;
import com.onlineshop.framework.models.order.service.IOrderItemService;
import com.onlineshop.framework.models.order.service.IOrderService;
import com.onlineshop.framework.models.order.strategy.OrderBuildStrategy.OrderBuildResult;
import com.onlineshop.framework.models.order.strategy.OrderStrategyContext;
import com.onlineshop.framework.models.order.strategy.OrderValidateStrategy;
import com.onlineshop.framework.models.order.vo.OrderAggregateVO;
import com.onlineshop.framework.models.order.vo.OrderVO;
import com.onlineshop.framework.models.order.vo.StoreOrderItemVO;
import com.onlineshop.framework.models.order.vo.StoreOrderVO;
import com.onlineshop.framework.models.order.wrapper.OrderQueryWrapper;
import com.onlineshop.framework.models.store.IStoreService;
import com.onlineshop.framework.models.store.Store;
import com.onlineshop.framework.utils.AssertUtils;
import com.onlineshop.framework.utils.AuthUserUtils;
import com.onlineshop.framework.utils.DateTimeUtil;
import com.onlineshop.framework.utils.money.Money;
import com.onlineshop.framework.utils.money.MoneyUtil;

/**
 * 订单应用服务
 * 负责订单跨模块流程编排
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderAppService implements IOrderAppService {
    private final OrderStrategyContext orderStrategyContext;
    private final IOrderService orderService;
    private final IOrderItemService orderItemService;
    private final IAddressService addressService;
    private final IStoreService storeService;
    private final IGoodsService goodsService;
    private final IGoodsSkuService goodsSkuService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderCreateResultDTO createOrder(TradeDTO tradeDTO, CartType cartType) {
        Address address = loadValidateAddress(tradeDTO);

        OrderValidateStrategy validateStrategy = orderStrategyContext.getValidateStrategy(cartType);
        validateStrategy.validate(tradeDTO);

        OrderBuildStrategy buildStrategy = orderStrategyContext.getBuildStrategy(cartType);
        OrderBuildResult orderBuildResult = buildStrategy.buildOrders(tradeDTO, address);
        AssertUtils.notNull(orderBuildResult, BizErrorCode.ORDER_CREATE_FAILED);

        OrderCreateResultDTO result = processOrderAggregate(orderBuildResult);
        log.info("订单创建成功, orderNo: {}", result.getOrderNo());
        pushCleanCartGoodsEvent(tradeDTO.getTradeItems());
        pushOrderTimeoutCancelEvent(orderBuildResult.getPayOrder());
        return result;
    }

    @Override
    public IPage<OrderAggregateVO> pageQueryOrdersForClient(OrderParamsDTO queryDTO) {
        Page<Order> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        LambdaQueryWrapper<Order> wrapper = OrderQueryWrapper.build(queryDTO);

        IPage<Order> orderPage = orderService.page(page, wrapper);
        return orderPage.convert(this::createOrderAggregateVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean queryPaymentStatus(String orderNo) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            log.error("支付状态查询被中断, orderNo: {}", orderNo, e);
            Thread.currentThread()
                  .interrupt();
        }

        log.info("支付成功（模拟）, orderNo: {}, 支付状态: 成功", orderNo);
        Order order = orderService.queryByOrderNo(orderNo);
        AssertUtils.notNull(order, BizErrorCode.ORDER_NOT_EXIST);
        return orderService.updateOrderStatus(order, OrderStatus.PAID);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductInventory(String orderNo) {
        List<OrderItem> orderItems = getOrderItemsByOrderNo(orderNo);
        if (CollUtil.isEmpty(orderItems)) {
            log.warn("订单明细为空，无需扣减库存, orderNo: {}", orderNo);
            return;
        }

        for (OrderItem orderItem : orderItems) {
            Long skuId = orderItem.getSkuId();
            Integer quantity = orderItem.getQuantity();
            goodsSkuService.deductInventoryAndIncreaseSales(skuId, quantity);
            goodsService.increaseSales(orderItem.getGoodsId(), quantity);
        }
        log.info("订单库存扣减和销量更新完成, orderNo: {}, 明细数量: {}", orderNo, orderItems.size());
    }

    public OrderAggregateVO queryOrderDetail(String orderNo) {
        Order order = orderService.queryByOrderNo(orderNo);
        AssertUtils.notNull(order, BizErrorCode.ORDER_NOT_EXIST);

        if (OrderType.PARENT == OrderType.of(order.getOrderType())) {
            List<Order> subOrders = orderService.querySubOrders(order.getId());
            List<Long> storeId = subOrders.stream()
                                          .map(Order::getStoreId)
                                          .toList();
            Map<Long, String> storeIdMap = storeService.lambdaQuery()
                                                       .in(Store::getId, storeId)
                                                       .list()
                                                       .stream()
                                                       .collect(Collectors.toMap(Store::getId, Store::getName));

            List<StoreOrderVO> storeOrderVOS = new ArrayList<>();
            for (Order subOrder : subOrders) {
                List<StoreOrderItemVO> storeOrderItemVOS = orderItemService.listByOrderId(subOrder.getId())
                                                                           .stream()
                                                                           .map(this::buildStoreOrderItemVO)
                                                                           .toList();

                StoreOrderVO storeOrderVO = buildStoreOrderVO(storeOrderItemVOS,
                                                              subOrder,
                                                              storeIdMap.get(subOrder.getStoreId()));
                storeOrderVOS.add(storeOrderVO);
            }
            return buildOrderAggregateVO(order, storeOrderVOS);
        }

        Store store = storeService.getById(order.getStoreId());
        List<StoreOrderItemVO> storeOrderItemVOS = orderItemService.listByOrderId(order.getId())
                                                                   .stream()
                                                                   .map(this::buildStoreOrderItemVO)
                                                                   .toList();
        StoreOrderVO storeOrderVO = buildStoreOrderVO(storeOrderItemVOS, order, store.getName());
        return buildOrderAggregateVO(order, Collections.singletonList(storeOrderVO));
    }

    @Override
    public Order queryOrder(Long orderId) {
        return orderService.getById(orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(OrderCancelDTO cancelDTO) {
        Order order = queryCancelableOrder(cancelDTO.getOrderNo());
        AssertUtils.notNull(order, BizErrorCode.ORDER_NOT_EXIST);
        order.setReason(cancelDTO.getReason());
        return orderService.updateOrderStatus(order, OrderStatus.CANCELED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean finishOrder(String orderNo) {
        Order order = orderService.queryUserOrderByOrderNo(orderNo);
        AssertUtils.notNull(order, BizErrorCode.ORDER_NOT_EXIST);
        if (!orderService.updateOrderStatus(order, OrderStatus.FINISHED)) {
            return false;
        }

        deductInventory(orderNo);
        return true;
    }

    @Override
    public IPage<OrderVO> pageQueryOrdersForAdmin(OrderParamsDTO queryDTO) {
        LambdaQueryWrapper<Order> wrapper = OrderQueryWrapper.build(queryDTO);

        Page<Order> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        IPage<Order> orderPage = orderService.page(page, wrapper);
        return orderPage.convert(OrderVO::buildOrderVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean shipOrder(String orderNo) {
        Order order = queryStoreOrder(orderNo);
        AssertUtils.notNull(order, BizErrorCode.ORDER_NOT_EXIST);
        return orderService.updateOrderStatus(order, OrderStatus.SHIPPED);
    }

    @Override
    public OrderVO queryOrderComment(String orderNo) {
        Order order = queryStoreOrder(orderNo);
        AssertUtils.notNull(order, BizErrorCode.ORDER_NOT_EXIST);
        return OrderVO.buildOrderVO(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean autoReceiveOrder(Order order) {
        if (!DateTimeUtil.isExpired(order.getCreateTime()
                                         .plusMinutes(30L))
                || !OrderStatus.SHIPPED.getCode()
                                       .equals(order.getStatus())) {
            return false;
        }
        return orderService.updateOrderStatus(order, OrderStatus.FINISHED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int autoReceiveShippedOrders() {
        List<Order> shippedOrders = orderService.queryShippedOrders();
        if (CollUtil.isEmpty(shippedOrders)) {
            return 0;
        }

        int receivedCount = 0;
        for (Order order : shippedOrders) {
            try {
                if (autoReceiveOrder(order)) {
                    receivedCount++;
                }
            } catch (Exception e) {
                log.error("订单自动收货失败，订单ID: {}, 订单号: {}", order.getId(), order.getNo(), e);
            }
        }
        return receivedCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int closeTimeoutCreatedOrders() {
        List<Order> timeoutOrders = orderService.queryTimeoutCreatedOrders(LocalDateTime.now()
                                                                                        .minusMinutes(30));
        if (CollUtil.isEmpty(timeoutOrders)) {
            return 0;
        }

        int closedCount = 0;
        for (Order order : timeoutOrders) {
            try {
                closeOrder(order);
                closedCount++;
            } catch (Exception e) {
                log.error("订单关闭消息补偿发送失败，订单ID：{}", order.getId(), e);
            }
        }
        return closedCount;
    }

    private Order queryStoreOrder(String orderNo) {
        Long storeId = AuthUserUtils.getStoreId();
        AssertUtils.notNull(storeId, BizErrorCode.MERCHANT_NO_SHOP);
        return orderService.queryByOrderNoAndStoreIds(orderNo, Collections.singletonList(storeId));
    }

    private void closeOrder(Order order) {
        orderService.updateOrderStatus(order, OrderStatus.CLOSED);
    }

    private Order queryCancelableOrder(String orderNo) {
        if (AuthUserUtils.isMerchantAccount()) {
            Long storeId = AuthUserUtils.getStoreId();
            log.info("商家取消订单, orderNo: {}, storeId: {}", orderNo, storeId);
            return queryStoreOrder(orderNo);
        }

        Long userId = AuthUserUtils.getUserId();
        log.info("用户取消订单, orderNo: {}, userId: {}", orderNo, userId);
        return orderService.queryUserOrderByOrderNo(orderNo);
    }

    private List<OrderItem> getOrderItemsByOrderNo(String orderNo) {
        Order order = orderService.queryByOrderNo(orderNo);
        AssertUtils.notNull(order, BizErrorCode.ORDER_NOT_EXIST);

        List<Long> orderIds = new ArrayList<>();
        orderIds.add(order.getId());
        if (OrderType.PARENT == OrderType.of(order.getOrderType())) {
            orderIds = orderService.querySubOrders(order.getId())
                                   .stream()
                                   .map(Order::getId)
                                   .toList();
        }

        return orderItemService.lambdaQuery()
                               .in(OrderItem::getOrderId, orderIds)
                               .list();
    }

    private OrderAggregateVO createOrderAggregateVO(Order topOrder) {
        List<StoreOrderVO> storeOrders = new ArrayList<>();
        if (OrderType.PARENT.getCode()
                            .equals(topOrder.getOrderType())) {
            List<Order> subOrders = orderService.querySubOrders(topOrder.getId());
            List<Long> storeIds = subOrders.stream()
                                           .map(Order::getStoreId)
                                           .distinct()
                                           .toList();

            Map<Long, String> storeNameMap = storeService.listByIds(storeIds)
                                                         .stream()
                                                         .collect(Collectors.toMap(Store::getId, Store::getName));

            for (Order subOrder : subOrders) {
                List<StoreOrderItemVO> items = orderItemService.lambdaQuery()
                                                               .eq(OrderItem::getOrderId, subOrder.getId())
                                                               .list()
                                                               .stream()
                                                               .map(this::buildStoreOrderItemVO)
                                                               .toList();

                StoreOrderVO storeOrderVO = buildStoreOrderVO(items,
                                                              subOrder,
                                                              storeNameMap.get(subOrder.getStoreId()));
                storeOrders.add(storeOrderVO);
            }
        } else {
            Store store = storeService.getById(topOrder.getStoreId());
            List<StoreOrderItemVO> items = orderItemService.lambdaQuery()
                                                           .eq(OrderItem::getOrderId, topOrder.getId())
                                                           .list()
                                                           .stream()
                                                           .map(this::buildStoreOrderItemVO)
                                                           .toList();

            StoreOrderVO shopOrderVO = buildStoreOrderVO(items, topOrder, store.getName());
            storeOrders.add(shopOrderVO);
        }

        return buildOrderAggregateVO(topOrder, storeOrders);
    }

    private StoreOrderItemVO buildStoreOrderItemVO(OrderItem item) {
        Map<String, String> selectedSpecs = parseSkuSpecs(item.getSkuSpecs());

        return StoreOrderItemVO.builder()
                               .orderItemId(item.getId())
                               .goodsId(item.getGoodsId())
                               .goodsName(item.getGoodsName())
                               .goodsMainImageUrl(item.getGoodsMainImageUrl())
                               .goodsPrice(Money.ofCents(item.getGoodsPrice())
                                                .toYuanString())
                               .quantity(item.getQuantity())
                               .totalPrice(Money.ofCents(item.getTotalPrice())
                                                .toYuanString())
                               .commentStatus(item.getCommentStatus())
                               .selectedSpecs(selectedSpecs)
                               .build();
    }

    private StoreOrderVO buildStoreOrderVO(List<StoreOrderItemVO> items, Order order, String storeName) {
        return StoreOrderVO.builder()
                           .orderNo(order.getNo())
                           .storeId(order.getStoreId())
                           .storeName(storeName)
                           .status(order.getStatus())
                           .items(items)
                           .totalPrice(calculateOrderTotalPrice(items))
                           .count(calculateOrderGoodsTotalNum(items))
                           .build();
    }

    private OrderAggregateVO buildOrderAggregateVO(Order topOrder, List<StoreOrderVO> storeOrders) {
        return OrderAggregateVO.builder()
                               .orderNo(topOrder.getNo())
                               .status(topOrder.getStatus())
                               .createTime(topOrder.getCreateTime())
                               .expireTime(topOrder.getCreateTime()
                                                   .plusMinutes(30L))
                               .reason(topOrder.getReason())
                               .storeOrders(storeOrders)
                               .totalPrice(calculateAggregateOrderTotalPrice(storeOrders))
                               .count(calculateAggregateOrderGoodsTotalNum(storeOrders))
                               .build();
    }

    private Map<String, String> parseSkuSpecs(String skuSpecs) {
        if (StrUtil.isBlank(skuSpecs)) {
            return Collections.emptyMap();
        }

        Map<String, String> specMap = new LinkedHashMap<>();
        String[] specPairs = skuSpecs.split(";");
        for (String pair : specPairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                specMap.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }
        return specMap;
    }

    private String calculateOrderTotalPrice(List<StoreOrderItemVO> items) {
        List<Money> monies = items.stream()
                                  .map(item -> Money.ofYuan(item.getTotalPrice()))
                                  .toList();
        return MoneyUtil.sum(monies)
                        .toYuanString();
    }

    private Long calculateOrderGoodsTotalNum(List<StoreOrderItemVO> items) {
        return items.stream()
                    .mapToLong(StoreOrderItemVO::getQuantity)
                    .sum();
    }

    private String calculateAggregateOrderTotalPrice(List<StoreOrderVO> storeOrders) {
        List<Money> monies = storeOrders.stream()
                                        .map(order -> Money.ofYuan(order.getTotalPrice()))
                                        .toList();
        return MoneyUtil.sum(monies)
                        .toYuanString();
    }

    private Long calculateAggregateOrderGoodsTotalNum(List<StoreOrderVO> items) {
        return items.stream()
                    .mapToLong(StoreOrderVO::getCount)
                    .sum();
    }

    private @NonNull Address loadValidateAddress(TradeDTO tradeDTO) {
        Address address = getAddress(tradeDTO);
        AssertUtils.notNull(address, BizErrorCode.ADDRESS_NOT_EXIST);
        return address;
    }

    private OrderCreateResultDTO processOrderAggregate(@NonNull OrderBuildStrategy.OrderBuildResult buildResults) {
        Order payOrder = buildResults.getPayOrder();
        orderService.savePayOrder(payOrder);

        List<Order> orders = buildResults.getSubOrders()
                                         .stream()
                                         .peek(build -> build.getOrder()
                                                             .setParentId(payOrder.getId()))
                                         .map(OrderBuildStrategy.RawOrderBuild::getOrder)
                                         .toList();
        orderService.saveOrders(orders);

        List<OrderItem> orderItems = buildResults.getSubOrders()
                                                 .stream()
                                                 .peek(this::supplementOrderItemOrderId)
                                                 .map(OrderBuildStrategy.RawOrderBuild::getOrderItems)
                                                 .flatMap(List::stream)
                                                 .toList();
        saveOrderItems(orderItems);

        return OrderCreateResultDTO.builder()
                                   .orderNo(payOrder.getNo())
                                   .expireTime(payOrder.getCreateTime()
                                                       .plusMinutes(30))
                                   .build();
    }

    private void pushCleanCartGoodsEvent(List<TradeShopDTO> tradeItems) {
        List<Long> skuIds = tradeItems.stream()
                                      .map(TradeShopDTO::getTradeShopItemList)
                                      .flatMap(Collection::stream)
                                      .map(TradeShopItemDTO::getSkuId)
                                      .toList();

        applicationEventPublisher.publishEvent(ClearCartEvent.builder()
                                                             .skuIds(skuIds)
                                                             .build());
    }

    private void pushOrderTimeoutCancelEvent(Order payOrder) {
        applicationEventPublisher.publishEvent(OrderTimeoutCancelEvent.builder()
                                                                      .orderId(payOrder.getId())
                                                                      .orderNo(payOrder.getNo())
                                                                      .build());
    }

    private Address getAddress(TradeDTO tradeDTO) {
        return addressService.lambdaQuery()
                             .eq(Address::getId, tradeDTO.getAddressId())
                             .eq(Address::getUserId, AuthUserUtils.getUserId())
                             .one();
    }

    private void supplementOrderItemOrderId(OrderBuildStrategy.RawOrderBuild orderBuild) {
        Order order = orderBuild.getOrder();
        for (OrderItem orderItem : orderBuild.getOrderItems()) {
            orderItem.setOrderId(order.getId());
        }
    }

    private void saveOrderItems(List<OrderItem> orderItems) {
        boolean isSuccess = orderItemService.saveBatchItems(orderItems);
        AssertUtils.isTrue(isSuccess, BizErrorCode.ORDER_CREATE_FAILED);
    }
}
