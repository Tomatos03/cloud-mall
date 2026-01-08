package com.onlineshop.framework.models.goods.sku;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("goods_sku")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GoodsSku implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 所属商品(spu)
     */
    private Long goodsId;
    
    /**
     * 售价(分)
     */
    private Long price;
    
    /**
     * 库存
     */
    private Long inventory;
    
    /**
     * 销量
     */
    private Long sales;
    
    /**
     * 状态, 1-上架, 0-下架
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
}