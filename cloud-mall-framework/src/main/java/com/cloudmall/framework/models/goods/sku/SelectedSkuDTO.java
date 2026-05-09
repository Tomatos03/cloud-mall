package com.cloudmall.framework.models.goods.sku;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/1/20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectedSkuDTO {
    private Long id;
    /**
     * 规格信息
     */
    @NotEmpty(message = "SKU规格信息不能为空")
    @Valid
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private List<Long> specValueIds;

    /**
     * 价格（整数，分为单位）
     */
    @NotNull(message = "SKU价格不能为空")
    private String price;

    /**
     * 库存数量
     */
    @NotNull(message = "SKU库存不能为空")
    private Long inventory;

    /**
     * 启用状态
     */
    @NotNull(message = "SKU状态不能为空")
    private Boolean status;
}

