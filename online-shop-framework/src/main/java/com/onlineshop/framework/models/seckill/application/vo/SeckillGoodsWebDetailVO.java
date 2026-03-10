package com.onlineshop.framework.models.seckill.application.vo;

import java.io.Serializable;
import java.math.BigDecimal;

import com.onlineshop.framework.models.goods.application.vo.WebGoodsDetailVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 秒杀商品聚合详情（秒杀信息 + SPU详情）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillGoodsWebDetailVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 秒杀商品ID
     */
    private Long seckillGoodsId;

    /**
     * 秒杀活动ID
     */
    private Long activityId;

    /**
     * 对应SPU商品ID
     */
    private Long goodsId;

    /**
     * 当前秒杀商品对应SKU
     */
    private Long selectedSkuId;

    /**
     * 秒杀商品名称快照
     */
    private String goodsName;

    /**
     * 秒杀商品主图快照
     */
    private String mainImageUrl;

    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;

    /**
     * 秒杀申报库存
     */
    private Integer stock;

    /**
     * 秒杀已售数量
     */
    private Integer soldCount;

    /**
     * 秒杀剩余库存
     */
    private Integer remainingStock;

    /**
     * Web端商品详情（SPU维度）
     */
    private WebGoodsDetailVO goodsDetail;
}
