package com.cloudmall.framework.models.audit.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 秒杀商品项目VO
 * 用于审核详情中显示单个商品信息
 *
 * @author Tomatos
 * @date 2026/3/6
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillGoodsItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品在列表中的序号（用于前端展示）
     */
    private Integer index;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;

    /**
     * 秒杀库存
     */
    private Integer stock;

    /**
     * 商品原价（用于前端展示对比）
     */
    private BigDecimal originalPrice;
}
