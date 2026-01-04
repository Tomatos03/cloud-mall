package com.onlineshop.framework.models.order.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/1/2
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StoreOrderItemVO implements Serializable {
    private Long goodsId;      // 商品ID
    private String goodsName;  // 商品名称快照
    private String goodsImg;   // 商品图片快照
    private Long goodsPrice;   // 下单时商品单价（单位：分）
    private String goodsPriceText; // 下单时商品单价（单位：元，格式化字符串）
    private Integer quantity;     // 购买数量
    private Long totalPrice;   // 明细小计（单位：分）
    private String totalPriceText; // 明细小计（单位：元，格式化字符串）
    private LocalDateTime createTime; // 创建时间
}