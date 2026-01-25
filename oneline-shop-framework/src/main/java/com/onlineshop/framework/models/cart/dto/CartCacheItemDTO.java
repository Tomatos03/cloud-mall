package com.onlineshop.framework.models.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 购物车缓存项DTO
 * 仅包含skuId，因为skuId能唯一确定购物车中的一个商品项
 *
 * @author : Tomatos
 * @date : 2025/12/25
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartCacheItemDTO {
    private Long skuId;
}