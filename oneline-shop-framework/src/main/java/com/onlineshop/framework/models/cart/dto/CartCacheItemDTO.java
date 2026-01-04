package com.onlineshop.framework.models.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 *
 * @author : Tomatos
 * @date : 2025/12/25
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartCacheItemDTO {
    private Long storeId;
    private Long goodsId;
}
