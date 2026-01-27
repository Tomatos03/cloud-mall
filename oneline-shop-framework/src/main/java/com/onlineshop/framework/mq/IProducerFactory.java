package com.onlineshop.framework.mq;

import com.onlineshop.framework.common.enums.BizType;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/1/26
 */
public interface IProducerFactory<T> {
    IProducer<T> getProducer(BizType bizType);
}
