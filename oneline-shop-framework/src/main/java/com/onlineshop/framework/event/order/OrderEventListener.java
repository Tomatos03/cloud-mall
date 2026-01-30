package com.onlineshop.framework.event.order;

import com.onlineshop.framework.common.enums.BizType;
import com.onlineshop.framework.models.order.entity.Order;
import com.onlineshop.framework.mq.IProducer;
import com.onlineshop.framework.mq.order.IOrderProducerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/1/27
 */
@Component
@RequiredArgsConstructor
public class OrderEventListener {
    private final IOrderProducerFactory orderProducerFactory;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCreated(OrderTimeoutCancelEvent event) {
        IProducer<Order> producer = orderProducerFactory.getProducer(BizType.ORDER_TIMEOUT_CLOSE);
        producer.send(
                Order.builder()
                     .id(event.getOrderId())
                     .no(event.getOrderNo())
                     .build()
        );
    }
}
