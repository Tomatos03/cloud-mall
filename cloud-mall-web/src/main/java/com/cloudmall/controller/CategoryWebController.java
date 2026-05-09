package com.cloudmall.controller;

import com.cloudmall.framework.models.category.Category;
import com.cloudmall.framework.models.category.ICategoryService;
import com.cloudmall.framework.models.category.vo.CategoryGoodsSectionVO;
import com.cloudmall.framework.models.goods.application.IGoodsAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *
 *
 * @author : Tomatos
 * @date : 2025/12/19
 */
@RestController
@RequestMapping("/web/category")
@RequiredArgsConstructor
public class CategoryWebController {
    private final ICategoryService categoryService;
    private final IGoodsAppService goodsAppService;

    @GetMapping("/list")
    public List<Category> getCategoryList() {
        return categoryService.getCategoryList();
    }

    @GetMapping("/category-goods")
    public List<CategoryGoodsSectionVO> getCategoryGoods() {
        return goodsAppService.getCategoryGoodsSections();
    }
}