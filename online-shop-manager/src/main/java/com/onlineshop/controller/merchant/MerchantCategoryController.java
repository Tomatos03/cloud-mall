package com.onlineshop.controller.merchant;

import com.onlineshop.framework.models.category.Category;
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
 * @date : 2025/12/28
 */
@RestController
@RequestMapping("manager/merchant/category")
public class MerchantCategoryController {
    @Autowired
    private ICategoryService categoryService;

    @GetMapping("/tree")
    public List<CategoryVO> getCategoryTree() {
        return categoryService.getCategoryTree();
    }

    @GetMapping("/list")
    public List<Category> getCategoryList() {
        return categoryService.getCategoryList();
    }
}