package com.onlineshop.framework.models.order.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

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
    @JsonProperty("id")
    private Long orderItemId;
    private Long goodsId;      // 商品ID
    private String goodsName;  // 商品名称快照
    private String goodsMainImageUrl;
    private String goodsPrice;   // 下单时商品单价（单位：分)
    private Integer quantity;     // 购买数量
    private String totalPrice;   // 明细小计（单位：分）
    private String originalPrice;   // 折扣前价格（元）
    private String discountAmount;  // 分摊优惠（元）
    private Boolean commentStatus; // 是否已评价
    private Map<String, String> selectedSpecs; // 购买时选择的规格，key: 规格名称，value: 规格值名称
}