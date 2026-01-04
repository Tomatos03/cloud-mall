package com.onlineshop.framework.models.cart.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

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
public class CartSotreVO implements Serializable {
    @JsonProperty("storeId")
    private Long storeId;

    @JsonProperty("storeName")
    private String storeName;

    @JsonProperty("items")
    private List<CartStoreItemVO> items;
}
