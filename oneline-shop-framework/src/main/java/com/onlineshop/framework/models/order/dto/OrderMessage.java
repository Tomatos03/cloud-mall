package com.onlineshop.framework.models.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/1/25
 */
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class OrderMessage {
    private Long orderId;
    private String orderNo;
    private String orderJson;
}
