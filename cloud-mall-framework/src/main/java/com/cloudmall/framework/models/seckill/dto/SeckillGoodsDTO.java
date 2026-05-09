package com.cloudmall.framework.models.seckill.dto;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 秒杀商品数据传输对象
 *
 * @author Tomatos
 * @date 2026/2/26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillGoodsDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 审核项ID
     */
    private Long auditItemId;

    /**
     * 状态（商家查询时为审核状态：PENDING/APPROVED/REJECTED）
     */
    private String status;

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品主图URL
     */
    private String mainImageUrl;

    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;

    /**
     * 秒杀库存
     */
    private Integer stock;
}
