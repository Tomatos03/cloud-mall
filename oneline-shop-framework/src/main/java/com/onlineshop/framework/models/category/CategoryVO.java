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
public class CategoryVO {
    private Long id;
    private String name;
    private Integer sort;
    private Boolean status;
    private List<CategoryVO> children;
}