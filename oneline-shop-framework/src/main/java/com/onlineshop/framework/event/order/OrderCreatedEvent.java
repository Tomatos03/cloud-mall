package com.onlineshop.framework.event.order;

import lombok.*;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/1/27
 */
@Data
@Builder
public class OrderCreatedEvent {
    private Long orderId;
    private final String orderNo;
}
