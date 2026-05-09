package com.cloudmall.framework.models.goods.spec.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("goods_sku_spec")
public class GoodsSkuSpec {
    @TableId(type = IdType.AUTO)
    private Long Id;
    
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