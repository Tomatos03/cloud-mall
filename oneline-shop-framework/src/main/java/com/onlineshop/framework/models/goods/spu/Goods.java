package com.onlineshop.framework.models.goods.spu;

import com.baomidou.mybatisplus.annotation.TableName;
import com.onlineshop.framework.common.entity.CommonDO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Data
@TableName("goods")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Goods extends CommonDO implements Serializable {
    private String name;
    private Long categoryId;
    private String categoryIdPath;
    private Long unitId;
    private String unitName;
    private String sellPoint;
    private String descriptionImages;
    private String displayImages;
    private Long storeId;
    private String storeName;
    private Integer sales;
    private Long minPrice;
    private Long maxPrice;
    private Boolean status;
}