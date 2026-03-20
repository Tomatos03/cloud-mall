package com.onlineshop.framework.mq.consumer.cart;

import cn.hutool.core.collection.CollUtil;
import com.onlineshop.framework.event.MQTag;
import com.onlineshop.framework.event.cart.ClearCartEvent;
import com.onlineshop.framework.models.cart.ICartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 购物车清理消费者
 *
 * @author : Tomatos
 * @date : 2026/3/15
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "rocketmq.name-server")
@RocketMQMessageListener(
        topic = "${mq.topic.cart}",
        selectorExpression = MQTag.CART_CLEAR,
        consumerGroup = "${mq.group.cart}"
)
public class CartClearConsumer implements RocketMQListener<ClearCartEvent> {
    private final ICartService cartService;

    @Override
    public void onMessage(ClearCartEvent message) {
        if (message == null || message.getUserId() == null || CollUtil.isEmpty(message.getSkuIds())) {
            log.warn("购物车清理消息无效, message: {}", message);
            return;
        }
        log.info("收到购物车清理消息, userId: {}, skuCount: {}",
                 message.getUserId(), message.getSkuIds().size());

        try {
            cartService.removeCartItems(message.getUserId(), message.getSkuIds());
            log.info("购物车清理成功, userId: {}, skuCount: {}", message.getUserId(), message.getSkuIds().size());
        } catch (Exception e) {
            log.error("购物车清理失败, userId: {}", message.getUserId(), e);
            throw new RuntimeException("处理购物车清理消息失败", e);
        }
    }
}
