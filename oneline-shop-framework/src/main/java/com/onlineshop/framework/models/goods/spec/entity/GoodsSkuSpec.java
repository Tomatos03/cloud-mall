package com.onlineshop.framework.models.goods.spec.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("goods_sku_spec")
public class GoodsSkuSpec {
    
    /**
     * sku id
     */
    private Long skuId;
    
    /**
     * 规格id
     */
    private Long specId;
    
    /**
     * 规格值id
     */
    private Long specValueId;
}