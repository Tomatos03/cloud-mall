package com.cloudmall.framework.application.order;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.collect.Lists;
import com.cloudmall.framework.common.enums.BizErrorCode;
import com.cloudmall.framework.models.cart.PurchaseMode;
import com.cloudmall.framework.models.coupon.application.ICouponAppService;
import com.cloudmall.framework.models.goods.sku.IGoodsSkuService;
import com.cloudmall.framework.models.goods.spu.IGoodsService;
import com.cloudmall.framework.application.order.creator.OrderCreatorFactory;
import com.cloudmall.framework.models.order.dto.OrderCancelDTO;
import com.cloudmall.framework.models.order.dto.OrderCreateResultDTO;
import com.cloudmall.framework.models.order.dto.OrderParamsDTO;
import com.cloudmall.framework.models.order.dto.TradeDTO;
import com.cloudmall.framework.models.order.entity.Order;
import com.cloudmall.framework.models.order.entity.OrderItem;
import com.cloudmall.framework.models.order.enums.OrderStatus;
import com.cloudmall.framework.models.order.service.IOrderItemService;
import com.cloudmall.framework.models.order.service.IOrderService;
import com.cloudmall.framework.assembler.OrderAggregateVOAssembler;
import com.cloudmall.framework.assembler.StoreOrderItemVOAssembler;
import com.cloudmall.framework.assembler.StoreOrderVOAssembler;
import com.cloudmall.framework.models.order.vo.OrderAggregateVO;
import com.cloudmall.framework.models.order.vo.OrderVO;
import com.cloudmall.framework.models.order.vo.StoreOrderItemVO;
import com.cloudmall.framework.models.order.vo.StoreOrderVO;
import com.cloudmall.framework.models.store.IStoreService;
import com.cloudmall.framework.models.store.Store;
import com.cloudmall.framework.models.store.utils.StoreUtil;
import com.cloudmall.framework.utils.AssertUtils;
import com.cloudmall.framework.context.AuthUserContext;
import com.cloudmall.framework.models.order.utils.OrderUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 订单应用服务
 * 负责订单跨模块流程编排
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderAppService implements IOrderAppService {
    private final OrderCreatorFactory orderCreatorFactory;
    private final IOrderService orderService;
    private final IOrderItemService orderItemService;
    private final IStoreService storeService;
    private final IGoodsService goodsService;
    private final IGoodsSkuService goodsSkuService;
    private final ICouponAppService couponAppService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderCreateResultDTO createOrder(TradeDTO tradeDTO, PurchaseMode purchaseMode) {
        OrderCreateResultDTO result = orderCreatorFactory.getOrderCreator(purchaseMode)
                                                         .create(tradeDTO);
        result.setPayQrCode(OrderCreateResultDTO.MOCK_PAY_QR_CODE);
        log.info("订单创建成功, orderNo: {}", result.getOrderNo());
        return result;
    }

    @Override
    public IPage<OrderAggregateVO> pageQueryOrdersForClient(OrderParamsDTO queryDTO) {
        IPage<Order> orderPage = orderService.pageQueryOrders(queryDTO);
        return orderPage.convert(this::createOrderAggregateVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void queryPaymentStatus(String orderNo) throws InterruptedException {
        TimeUnit.SECONDS.sleep(1);
        onPaymentSuccess(orderNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onPaymentSuccess(String orderNo) {
        log.info("支付成功（模拟）, orderNo: {}, 支付状态: 成功", orderNo);
        deductInventory(orderNo);
        couponAppService.useCoupon(orderNo);
        orderService.updateOrderStatus(orderNo, OrderStatus.PAID);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductInventory(String orderNo) {
        Order order = orderService.queryByOrderNo(orderNo);
        List<Long> orderIds = Lists.newArrayList(order.getId());
        if (order.isParent()) {
            orderIds.addAll(OrderUtil.toOrderIds(orderService.querySubOrders(order.getId())));
        }
        List<OrderItem> orderItems = orderItemService.listByOrderIds(orderIds);
        for (OrderItem orderItem : orderItems) {
            Long skuId = orderItem.getSkuId();
            Integer quantity = orderItem.getQuantity();
            goodsSkuService.deductInventory(skuId, quantity);
            goodsSkuService.increaseSales(skuId, quantity);
            goodsService.increaseSales(orderItem.getGoodsId(), quantity);
        }
        log.info("订单库存扣减和销量更新完成, orderNo: {}, 明细数量: {}", orderNo, orderItems.size());
    }

    public OrderAggregateVO queryOrderDetail(String orderNo) {
        Order order = orderService.queryByOrderNo(orderNo);
        AssertUtils.notNull(order, BizErrorCode.ORDER_NOT_EXIST);

        if (order.isParent()) {
            List<Order> subOrders = orderService.querySubOrders(order.getId());
            Map<Long, String> storeIdToStoreNameMap = StoreUtil.toIdToNameMap(storeService.listByIds(
                    OrderUtil.toStoreIds(subOrders)));

            List<StoreOrderVO> storeOrderVOS = new ArrayList<>();
            for (Order subOrder : subOrders) {
                List<StoreOrderItemVO> items = orderItemService.listByOrderId(subOrder.getId())
                                                               .stream()
                                                               .map(StoreOrderItemVOAssembler::assembler)
                                                               .toList();
                storeOrderVOS.add(StoreOrderVOAssembler.assemble(items, subOrder, storeIdToStoreNameMap.get(subOrder.getStoreId())));
            }
            return OrderAggregateVOAssembler.assemble(order, storeOrderVOS);
        }

        Store store = storeService.getById(order.getStoreId());
        List<StoreOrderItemVO> items = orderItemService.listByOrderId(order.getId())
                                                       .stream()
                                                       .map(StoreOrderItemVOAssembler::assembler)
                                                       .toList();
        return OrderAggregateVOAssembler.assemble(order, Collections.singletonList(StoreOrderVOAssembler.assemble(items, order, store.getName())));
    }

    @Override
    public Order queryOrder(Long orderId) {
        return orderService.getById(orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(OrderCancelDTO cancelDTO) {
        Order order = orderService.queryCancelableOrder(cancelDTO.getOrderNo());
        order.setReason(cancelDTO.getReason());
        orderService.updateOrderStatus(order, OrderStatus.CANCELED);
        couponAppService.releaseCoupon(order.getNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishOrder(String orderNo) {
        orderService.updateOrderStatus(orderNo, OrderStatus.FINISHED);
    }

    @Override
    public IPage<OrderVO> pageQueryOrdersForAdmin(OrderParamsDTO queryDTO) {
        IPage<Order> orderPage = orderService.pageQueryOrders(queryDTO);
        return orderPage.convert(OrderVO::buildOrderVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shipOrder(String orderNo) {
        Order order = orderService.queryStoreOrder(orderNo, AuthUserContext.getStoreId());
        orderService.updateOrderStatus(order, OrderStatus.SHIPPED);
    }

    @Override
    public OrderVO queryOrderComment(String orderNo) {
        Order order = orderService.queryStoreOrder(orderNo, AuthUserContext.getStoreId());
        AssertUtils.notNull(order, BizErrorCode.ORDER_NOT_EXIST);
        return OrderVO.buildOrderVO(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoReceiveOrder(Order order) {
        if (order.isExpired()) {
            return;
        }
        orderService.updateOrderStatus(order.getNo(), OrderStatus.FINISHED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoReceiveShippedOrders() {
        List<Order> shippedOrders = orderService.queryShippedOrders();
        if (CollUtil.isEmpty(shippedOrders)) {
            return;
        }
        shippedOrders.forEach(this::autoReceiveOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeTimeoutOrders() {
        LocalDateTime timeoutDeadline = LocalDateTime.now()
                                                     .minusMinutes(30L);
        List<Order> timeoutOrders = orderService.queryTimeoutOrders(timeoutDeadline);
        if (CollUtil.isEmpty(timeoutOrders)) {
            return;
        }
        timeoutOrders.forEach(order -> orderService.updateOrderStatus(order, OrderStatus.CLOSED));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refundOrder(String orderNo) {
        Order order = orderService.queryByOrderNo(orderNo);
        AssertUtils.notNull(order, BizErrorCode.ORDER_NOT_EXIST);

        if (order.isParent()) {
            List<Order> subOrders = orderService.querySubOrders(order.getId());
            for (Order subOrder : subOrders) {
                if (!subOrder.isCanceled()) {
                    orderService.updateOrderStatus(subOrder, OrderStatus.CANCELED);
                }
            }
            orderService.updateOrderStatus(order, OrderStatus.CANCELED);

            if (OrderUtil.isAllCanceled(subOrders) && order.getCouponId() != null) {
                couponAppService.releaseCoupon(order.getNo());
                log.info("全额退款，优惠券已退还, orderNo: {}", orderNo);
            } else {
                log.info("部分退款，优惠券不退还, orderNo: {}", orderNo);
            }
        } else {
            orderService.updateOrderStatus(order, OrderStatus.CANCELED);
            if (order.getCouponId() != null) {
                couponAppService.releaseCoupon(order.getNo());
            }
        }
        log.info("退款完成, orderNo: {}", orderNo);
    }

    private OrderAggregateVO createOrderAggregateVO(Order order) {
        List<StoreOrderVO> storeOrders = new ArrayList<>();
        if (order.isParent()) {
            List<Order> subOrders = orderService.querySubOrders(order.getId());
            List<Store> stores = storeService.listByIds(OrderUtil.toStoreIds(subOrders));
            Map<Long, String> storeNameMap = StoreUtil.toIdToNameMap(stores);

            for (Order subOrder : subOrders) {
                List<StoreOrderItemVO> items = orderItemService.listByOrderId(subOrder.getId())
                                                               .stream()
                                                               .map(StoreOrderItemVOAssembler::assembler)
                                                               .toList();
                storeOrders.add(StoreOrderVOAssembler.assemble(items, subOrder, storeNameMap.get(subOrder.getStoreId())));
            }
        } else {
            Store store = storeService.getById(order.getStoreId());
            List<StoreOrderItemVO> items = orderItemService.listByOrderId(order.getId())
                                                           .stream()
                                                           .map(StoreOrderItemVOAssembler::assembler)
                                                           .toList();
            storeOrders.add(StoreOrderVOAssembler.assemble(items, order, store.getName()));
        }

        return OrderAggregateVOAssembler.assemble(order, storeOrders);
    }

}
