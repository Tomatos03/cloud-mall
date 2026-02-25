package com.onlineshop.controller;

import com.onlineshop.framework.models.category.vo.CategoryNodeVO;
import com.onlineshop.framework.models.category.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/2/11
 */
@RequestMapping("/merchant/category")
@RestController
@RequiredArgsConstructor
public class CategoryMerchantController {
    private final ICategoryService categoryService;

    /**
     * 获取分类树/列表
     */
    @GetMapping("/tree")
    public List<CategoryNodeVO> getCategoryTree() {
        return categoryService.getCategoryTree();
    }

    /**
     * 获取分类列表
     */
    @GetMapping("/list")
    public List<CategoryNodeVO> list() {
        return categoryService.getCategoryTree();
    }
}
