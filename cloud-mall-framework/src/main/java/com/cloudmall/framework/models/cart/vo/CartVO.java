package com.cloudmall.framework.models.cart.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 购物车
 *
 * @author : Tomatos
 * @date : 2025/12/24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartVO {
    /**
     * 按店铺分组的购物车
     */
    @JsonProperty("storeList")
    private List<CartStoreVO> storeList;
}