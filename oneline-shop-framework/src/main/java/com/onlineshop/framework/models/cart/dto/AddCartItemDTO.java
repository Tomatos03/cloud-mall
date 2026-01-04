package com.onlineshop.framework.models.cart.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 *
 *
 * @author : Tomatos
 * @date : 2025/12/24
 */
@Data
public class AddCartItemDTO {
    private Long goodsId;
    private Long storeId;
    @Min(value = 1, message = "数量至少为1")
    private Long quantity;
}