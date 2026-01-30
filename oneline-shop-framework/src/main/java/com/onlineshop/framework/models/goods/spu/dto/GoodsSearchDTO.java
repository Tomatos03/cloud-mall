package com.onlineshop.framework.models.goods.spu.dto;

import com.onlineshop.framework.common.entity.PageQueryDTO;
import lombok.*;
import lombok.experimental.SuperBuilder;

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
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class GoodsSearchDTO extends PageQueryDTO {
    private Long storeId;
    private String keyword;
    private Long categoryId;
    private String minPrice;
    private String maxPrice;
    private Boolean isDesc; // 降序
    @Builder.Default
    private String sortType = "COMPREHENSIVE";
}