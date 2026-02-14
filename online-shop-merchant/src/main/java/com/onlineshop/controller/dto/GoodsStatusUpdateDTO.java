package com.onlineshop.controller.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品状态更新DTO
 *
 * @author Tomatos
 * @date 2026/2/11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsStatusUpdateDTO {
    /**
     * 商品状态：true=上架，false=下架
     */
    @NotNull(message = "商品状态不能为空")
    private Boolean status;
}

