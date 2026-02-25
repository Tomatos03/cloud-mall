package com.onlineshop.framework.models.category.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分类标签VO（二级分类）
 *
 * @author Tomatos
 * @date 2026/3/2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTabVO {
    private Long id;
    private String name;
}
