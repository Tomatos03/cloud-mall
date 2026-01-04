package com.onlineshop.framework.models.order.dto;

import lombok.Data;

/**
 * 一个商品的交易对应一个交易项
 *
 * @author : Tomatos
 * @date : 2025/12/24
 */
@Data
public class TradeShopItemDTO {
    private Long goodsId;

    private Integer quantity;
}
