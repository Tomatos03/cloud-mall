package com.onlineshop.framework.models.order.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户端订单聚合视图 VO
 * 用于展示父订单或普通订单，包含所有子订单和商品明细
 *
 * @author : Tomatos
 * @date : 2025/12/24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderAggregateVO implements Serializable {
    private String orderNo;
    
    /**
     * 订单状态
     */
    private String status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    private List<StoreOrderVO> storeOrders;

    private Long totalPrice;   // 订单总价（单位：分）
    private String totalPriceText; // 订单总价（单位：元，格式化
    private Long count; // 订单总商品数量
}