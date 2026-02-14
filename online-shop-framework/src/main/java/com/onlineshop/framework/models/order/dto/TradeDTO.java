package com.onlineshop.framework.models.order.dto;

import lombok.Data;

import java.util.List;

/**
 * 交易数据传输对象
 *
 * @author : Tomatos
 * @date : 2025/12/23
 */
@Data
public class TradeDTO {
    private Long addressId;

    List<TradeShopDTO> tradeItems;
}