package com.onlineshop.framework.models.cart.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 *
 *
 * @author : Tomatos
 * @date : 2025/12/24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartStoreItemVO implements Serializable {

    /**
     * 店铺ID
     */
    @JsonProperty("storeId")
    private Long storeId;

    /**
     * 店铺名称
     */
    @JsonProperty("storeName")
    private String storeName;

    /**
     * 商品ID
     */
    @JsonProperty("goodsId")
    private Long goodsId;

    /**
     * 商品名称
     */
    @JsonProperty("goodsName")
    private String goodsName;

    /**
     * 商品价格（单位：分）
     */
    @JsonProperty("price")
    private Long price;

    /**
     * 是否被选中（默认false）
     */
    @JsonProperty("selected")
    private Boolean selected;

    /**
     * 库存
     */
    @JsonProperty("inventory")
    private Long inventory;

    /**
     * 下单数量
     */
    @JsonProperty("quantity")
    private Long quantity;

    /**
     * 主图
     */
    @JsonProperty("mainImage")
    private String mainImage;

    /**
     * 价格计量单位
     */
    @JsonProperty("unit")
    private String unit;
}
