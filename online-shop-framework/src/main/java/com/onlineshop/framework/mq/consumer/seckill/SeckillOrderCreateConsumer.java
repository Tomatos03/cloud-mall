package com.onlineshop.framework.mq.consumer.seckill;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.event.MQTag;
import com.onlineshop.framework.models.goods.sku.GoodsSku;
import com.onlineshop.framework.models.goods.sku.IGoodsSkuService;
import com.onlineshop.framework.models.order.entity.Order;
import com.onlineshop.framework.models.order.entity.OrderItem;
import com.onlineshop.framework.models.order.enums.OrderStatus;
import com.onlineshop.framework.models.order.enums.OrderType;
import com.onlineshop.framework.models.order.service.IOrderItemService;
import com.onlineshop.framework.models.order.service.IOrderService;
import com.onlineshop.framework.models.seckill.entity.SeckillGoods;
import com.onlineshop.framework.models.seckill.entity.SeckillOrder;
import com.onlineshop.framework.models.seckill.enums.SeckillOrderStatus;
import com.onlineshop.framework.models.seckill.service.SeckillGoodsService;
import com.onlineshop.framework.models.seckill.service.SeckillOrderService;
import com.onlineshop.framework.utils.AssertUtils;
import com.onlineshop.framework.utils.money.Money;

/**
 * 秒杀下单消息消费者
 * 负责异步创建 orders / order_item 记录
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "rocketmq.name-server")
@RocketMQMessageListener(
        topic = "${mq.topic.seckill}",
        selectorExpression = MQTag.SECKILL_ORDER_CREATE,
        consumerGroup = "${mq.group.seckill}"
)
public class SeckillOrderCreateConsumer implements RocketMQListener<Long> {
    private final IGoodsSkuService goodsSkuService;
    private final IOrderItemService orderItemService;
    private final IOrderService orderService;
    private final SeckillGoodsService seckillGoodsService;
    private final SeckillOrderService seckillOrderService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onMessage(Long seckillOrderId) {
        if (seckillOrderId == null) {
            log.warn("秒杀订单创建消息无效, seckillOrderId: null");
            return;
        }
        log.info("收到秒杀订单创建消息, seckillOrderId: {}", seckillOrderId);

        try {
            createOrder(seckillOrderId);
        } catch (Exception e) {
            log.error("处理秒杀订单创建消息失败, seckillOrderId: {}", seckillOrderId, e);
            throw new RuntimeException("处理秒杀订单创建消息失败", e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void createOrder(Long seckillOrderId) {
        log.info("开始异步建秒杀订单, seckillOrderId: {}", seckillOrderId);
        SeckillOrder seckillOrder = seckillOrderService.getById(seckillOrderId);
        if (shouldSkipCreateOrder(seckillOrderId, seckillOrder)) {
            return;
        }

        SeckillGoods seckillGoods = seckillGoodsService.getById(seckillOrder.getGoodsId());
        AssertUtils.notNull(seckillGoods, BizErrorCode.SECKILL_GOODS_NOT_FOUND);

        GoodsSku sku = goodsSkuService.getById(seckillGoods.getSkuId());
        AssertUtils.notNull(sku, BizErrorCode.PRODUCT_NOT_FOUND);

        Money seckillPrice = Money.ofYuan(seckillOrder.getPrice());
        long goodsPrice = seckillPrice.getCents();
        long totalPrice = seckillPrice.mul(seckillOrder.getQuantity()).getCents();

        Order order = saveOrder(seckillOrder, sku, totalPrice);
        saveOrderItem(seckillOrder, seckillGoods, sku, order.getId(), goodsPrice, totalPrice);
        updateGoodsSaleData(seckillOrder, seckillGoods, sku);

        log.info("秒杀订单异步建单成功, seckillOrderId: {}, orderId: {}, orderNo: {}",
                 seckillOrderId, order.getId(), order.getNo());
    }

    private boolean shouldSkipCreateOrder(Long seckillOrderId, SeckillOrder seckillOrder) {
        if (seckillOrder == null) {
            log.warn("秒杀订单不存在，忽略消息, seckillOrderId: {}", seckillOrderId);
            return true;
        }

        if (!SeckillOrderStatus.UNPAID.getCode().equals(seckillOrder.getStatus())) {
            log.info("秒杀订单状态不是未支付，忽略消息, seckillOrderId: {}, status: {}",
                     seckillOrderId, seckillOrder.getStatus());
            return true;
        }

        Order existedOrder = orderService.queryByOrderNo(seckillOrder.getOrderNo());
        if (existedOrder != null) {
            log.info("秒杀订单已完成异步建单，忽略重复消息, seckillOrderId: {}, orderNo: {}",
                     seckillOrderId, seckillOrder.getOrderNo());
            return true;
        }
        return false;
    }

    private Order saveOrder(SeckillOrder seckillOrder, GoodsSku sku, long totalPrice) {
        Order order = Order.builder()
                           .no(seckillOrder.getOrderNo())
                           .userId(seckillOrder.getUserId())
                           .storeId(sku.getStoreId())
                           .quantity(seckillOrder.getQuantity())
                           .totalPrice(totalPrice)
                           .userName("用户" + seckillOrder.getUserId())
                           .address("")
                           .phone("")
                           .status(OrderStatus.CREATED.getCode())
                           .orderType(OrderType.NORMAL.getCode())
                           .build();
        boolean orderSaved = orderService.save(order);
        AssertUtils.isTrue(orderSaved, BizErrorCode.ORDER_CREATE_FAILED);
        return order;
    }

    private void saveOrderItem(SeckillOrder seckillOrder,
                               SeckillGoods seckillGoods,
                               GoodsSku sku,
                               Long orderId,
                               long goodsPrice,
                               long totalPrice) {
        OrderItem orderItem = OrderItem.builder()
                                       .orderId(orderId)
                                       .skuId(sku.getId())
                                       .goodsId(sku.getGoodsId())
                                       .goodsName(seckillGoods.getGoodsName())
                                       .goodsMainImageUrl(seckillGoods.getMainImageUrl())
                                       .goodsPrice(goodsPrice)
                                       .quantity(seckillOrder.getQuantity())
                                       .totalPrice(totalPrice)
                                       .skuSpecs(sku.getSpecSnapshot())
                                       .commentStatus(Boolean.FALSE)
                                       .build();
        boolean orderItemSaved = orderItemService.save(orderItem);
        AssertUtils.isTrue(orderItemSaved, BizErrorCode.ORDER_CREATE_FAILED);
    }

    private void updateGoodsSaleData(SeckillOrder seckillOrder, SeckillGoods seckillGoods, GoodsSku sku) {
        seckillGoodsService.lambdaUpdate()
                           .eq(SeckillGoods::getId, seckillGoods.getId())
                           .setSql("sold_count = IFNULL(sold_count, 0) + " + seckillOrder.getQuantity())
                           .update();

        goodsSkuService.deductInventory(sku.getId(), seckillOrder.getQuantity());
        goodsSkuService.increaseSales(sku.getId(), seckillOrder.getQuantity());
    }
}
