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

    // ========== 搜索条件 ==========
    private String keyword;
    
    /**
     * 分类ID
     * 如果为null则查询所有分类
     */
    private Long categoryId;

    /**
     * 排序类型字符串（用于Spring自动绑定）
     * Spring会自动将请求参数转换为排序类型枚举
     */
    @Builder.Default
    private String sortType = "COMPREHENSIVE";
}