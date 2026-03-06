package com.onlineshop.framework.models.audit.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 秒杀商品项目
 * 代表一个秒杀商品申请中的单个商品
 *
 * 秒杀商品申请采用统一的List模型：
 * 无论提交1个还是N个商品，都使用List<SeckillGoodsItem>封装
 *
 * @author Tomatos
 * @date 2026/3/6
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillGoodsItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;

    /**
     * 秒杀库存
     */
    private Integer stock;
}
