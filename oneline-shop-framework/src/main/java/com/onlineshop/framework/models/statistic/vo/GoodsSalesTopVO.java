package com.onlineshop.framework.models.statistic.vo;

import com.onlineshop.framework.utils.image.Image;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 *
 * @author : Tomatos
 * @date : 2025/12/29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsSalesTopVO {
    private Integer rank;

    /**
     * 商品ID
     */
    private Long goodsId;

    /**
     * 商品名称
     */
    private String goodsName;

    private Image mainImage;
    /**
     * 销量（核心排序指标）
     */
    private Long saleCount;

    /**
     * 销售额（辅助展示，不参与主排序）
     */
    private String saleAmount;
}
