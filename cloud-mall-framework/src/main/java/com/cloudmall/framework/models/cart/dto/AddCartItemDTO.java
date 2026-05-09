package com.cloudmall.framework.models.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 添加购物车项DTO
 * 前端仅需传入SKU ID和购买数量，其他信息由后端从数据库构建
 *
 * @author : Tomatos
 * @date : 2025/12/24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddCartItemDTO {
    
    /**
     * SKU ID - 必需
     */
    @NotNull(message = "SKU ID不能为空")
    private Long skuId;

    /**
     * 购买数量 - 必需
     */
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为1")
    private Long quantity;
}