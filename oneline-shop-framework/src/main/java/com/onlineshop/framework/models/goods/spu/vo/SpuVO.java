package com.onlineshop.framework.models.goods.spu.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/1/12
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpuVO implements Serializable {
    private Long goodsId;
    private String goodsName;
    private String storeName;
    private List<String> displayImageUrls;
    private Long categoryId;
    private List<Integer> categoryIdPath; // 1/2/3级分类ID路径
    private String minPrice;
    private String maxPrice;
    private Boolean status;
    private String sellPoint;
    private String unitName;
    private Long unitId;
}