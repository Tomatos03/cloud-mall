package com.cloudmall.framework.models.order.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 店铺订单视图 VO
 * 用于展示单个店铺的订单信息和商品明细
 *
 * @author : Tomatos
 * @date : 2025/12/24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreOrderVO implements Serializable {
    /**
     * 订单号（子订单号或普通订单号）
     */
    private String orderNo;

    /**
     * 店铺ID
     */
    private Long storeId;

    /**
     * 店铺名称
     */
    private String storeName;

    /**
     * 订单状态
     */
    private String status;

    private String totalPrice;   // 订单总价（单位：分）
    private String couponDiscount;
    private String payAmount;
    private Long count; // 商品总数量

    /**
     * 订单商品明细列表
     */
    private List<StoreOrderItemVO> items;
}
