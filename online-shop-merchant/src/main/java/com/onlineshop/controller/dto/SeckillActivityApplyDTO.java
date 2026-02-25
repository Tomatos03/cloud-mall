package com.onlineshop.controller.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 秒杀活动申请DTO
 * 商家提交秒杀活动申请时使用
 *
 * @author Tomatos
 * @date 2026/2/26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillActivityApplyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 秒杀活动ID
     */
    @NotNull(message = "秒杀活动ID不能为空")
    private Long activityId;

    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /**
     * 秒杀价格
     */
    @NotNull(message = "秒杀价格不能为空")
    @DecimalMin(value = "0.01", message = "秒杀价格必须大于0")
    private BigDecimal seckillPrice;

    /**
     * 秒杀库存
     */
    @NotNull(message = "秒杀库存不能为空")
    @Min(value = 1, message = "秒杀库存必须至少为1")
    private Integer stock;
}
