package com.cloudmall.framework.models.order.dto;

import lombok.Data;

import java.util.List;

/**
 *
 *
 * @author : Tomatos
 * @date : 2025/12/24
 */
@Data
public class TradeShopDTO {
    private Long storeId;
    private Long userCouponId;
    private List<TradeShopItemDTO> tradeShopItemList;
}
