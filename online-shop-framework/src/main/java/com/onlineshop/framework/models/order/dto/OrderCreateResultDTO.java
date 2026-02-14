package com.onlineshop.framework.models.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 订单创建结果DTO
 *
 * @author : Tomatos
 * @date : 2025/12/23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateResultDTO {
    /**
     * 订单编号/父订单号
     */
    private String orderNo;
    private LocalDateTime expireTime;
}