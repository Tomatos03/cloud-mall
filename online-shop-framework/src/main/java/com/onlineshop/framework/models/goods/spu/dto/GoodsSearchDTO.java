package com.onlineshop.framework.models.goods.spu.dto;

import com.onlineshop.framework.common.entity.PageParamsDTO;
import lombok.*;

/**
 * 商品搜索 DTO
 * 封装商品搜索的所有参数，包括分页、关键词、排序、分类等
 *
 * @author : Tomatos
 * @date : 2025/1/1
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GoodsSearchDTO extends PageParamsDTO {
    private Long storeId;
    private String keyword;
    private Long categoryId;
    private String minPrice;
    private String maxPrice;
    private Boolean isDesc; // 降序
    private String sortType = "comprehensive";
}
