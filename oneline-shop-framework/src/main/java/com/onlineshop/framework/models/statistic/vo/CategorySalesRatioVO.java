package com.onlineshop.framework.models.statistic.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 *
 *
 * @author : Tomatos
 * @date : 2025/12/29
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategorySalesRatioVO {

    /**
     * 类目ID
     */
    private Long categoryId;

    /**
     * 类目名称
     */
    private String categoryName;

    /**
     * 类目销售额
     */
    private BigDecimal saleAmount;

    /**
     * 销售额占比（0 ~ 1，例如：0.235）
     */
    private BigDecimal saleRatio;
}
