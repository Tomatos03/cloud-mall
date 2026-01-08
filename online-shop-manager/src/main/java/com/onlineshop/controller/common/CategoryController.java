package com.onlineshop.controller.common;

import com.onlineshop.framework.models.category.CategoryVO;
import com.onlineshop.framework.models.category.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/1/5
 */
@RequestMapping("/manager/category")
@RestController
public class CategoryController {
    @Autowired
    private ICategoryService categoryService;

    @GetMapping("/tree")
    public List<CategoryVO> getCategoryTree() {
        return categoryService.getCategoryTree();
    }
}