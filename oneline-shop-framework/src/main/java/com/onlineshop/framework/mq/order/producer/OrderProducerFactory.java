package com.onlineshop.framework.mq.order.producer;

import com.onlineshop.framework.common.enums.BizType;
import com.onlineshop.framework.models.order.entity.Order;
import com.onlineshop.framework.mq.IProducer;
import com.onlineshop.framework.mq.order.IOrderProducer;
import com.onlineshop.framework.mq.order.IOrderProducerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/1/26
 */
@Component
public class OrderProducerFactory implements IOrderProducerFactory {
    private static volatile Map<String, IOrderProducer> ORDERPRODUCER_MAP;
    private final List<IOrderProducer> producers;

    @Autowired
    public OrderProducerFactory(List<IOrderProducer> producers) {
        this.producers = producers;
    }

    @Override
    public IProducer<Order> getProducer(BizType bizType) {
        ensureInit();
        return ORDERPRODUCER_MAP.get(bizType.name());
    }

    private void ensureInit() {
        if (ORDERPRODUCER_MAP != null) {
            return;
        }

        synchronized (OrderProducerFactory.class) {
            if (ORDERPRODUCER_MAP != null) {
                return;
            }
            ORDERPRODUCER_MAP = new HashMap<>();
            for (IOrderProducer producer : producers) {
                ORDERPRODUCER_MAP.put(producer.getSupportBizType().name(), producer);
            }
        }
    }
}
