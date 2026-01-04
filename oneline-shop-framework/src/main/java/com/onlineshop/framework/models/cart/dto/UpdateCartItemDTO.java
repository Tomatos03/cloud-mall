package com.onlineshop.framework.models.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 *
 *
 * @author : Tomatos
 * @date : 2025/12/24
 */
@Data
public class UpdateCartItemDTO {
    @NotNull(message = "店铺ID不能为空")
    private Long storeId;

    @NotNull(message = "商品ID不能为空")
    private Long goodsId;

    /**
     * 数量
     */
    @Min(value = 1, message = "数量至少为1")
    private Long quantity;
}
