package com.onlineshop.framework.models.goods.sku;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
     * 商品名称快照（冗余字段）
     */
    private String goodsName;

    /**
     * 商品主图URL快照（冗余字段）
     */
    private String mainImageUrl;

    /**
     * 店铺ID快照（冗余字段）
     */
    private Long storeId;

    /**
     * SKU规格组合快照（示例：红 XL 512G）
     */
    private String specSnapshot;

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
    private Boolean status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
