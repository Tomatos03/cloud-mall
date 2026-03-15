package com.onlineshop.framework.models.order.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 交易数据传输对象
 *
 * @author : Tomatos
 * @date : 2025/12/23
 */
@Data
public class TradeDTO {
    @NotNull
    private Long addressId;

    @NotEmpty
    @Valid
    private List<TradeShopDTO> tradeItems;
}
