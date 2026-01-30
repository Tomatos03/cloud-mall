package com.onlineshop.framework.models.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单取消DTO
 * 用于接收订单取消请求的参数
 *
 * @author : Tomatos
 * @date 2026/01/28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancelDTO {

    private String orderNo;

    private String reason;
}
