package com.onlineshop.framework.models.category;

import lombok.Data;

import java.util.List;

/**
 * 商品分类VO
 *
 * @author Tomatos
 * @date 2025/12/20
 */
@Data
public class CategoryNodeVO {
    private Long id;
    private String name;
    private Integer sort;
    private Integer level;
    private Boolean status;
    private List<CategoryNodeVO> children;
}