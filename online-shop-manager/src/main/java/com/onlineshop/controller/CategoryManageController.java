package com.onlineshop.controller;

import com.onlineshop.framework.models.category.Category;
import com.onlineshop.framework.models.category.ICategoryService;
import com.onlineshop.framework.models.category.vo.CategoryNodeVO;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类管理控制器
 * 合并自 admin/CategoryManagerController + merchant/MerchantCategoryController
 *
 * @author Tomatos
 * @date 2025/12/20
 */
@RestController
@RequestMapping("/manager/category")
@PreAuthorize("hasAuthority('category:view')")
public class CategoryManageController {

    @Autowired
    private ICategoryService categoryService;

    /**
     * 获取所有分类树（包含禁用）
     * 来自 admin/CategoryManagerController
     */
    @GetMapping("/allTree")
    public List<CategoryNodeVO> getAllCategoryTree() {
        return categoryService.getAllCategoryTree();
    }

    /**
     * 获取分类列表
     */
    @GetMapping("/list")
    public List<CategoryNodeVO> list() {
        return categoryService.getCategoryTree();
    }

    /**
     * 获取分类树/列表
     */
    @GetMapping("/tree")
    public List<CategoryNodeVO> getCategoryTree() {
        return categoryService.getCategoryTree();
    }

    /**
     * 添加分类
     * 来自 admin/CategoryManagerController
     */
    @PostMapping("/add")
    @PreAuthorize("hasAuthority('category:add')")
    public boolean add(@RequestBody Category category) {
        categoryService.addCategory(category);
        return true;
    }

    /**
     * 更新分类
     * 来自 admin/CategoryManagerController
     */
    @PostMapping("/update/{id}")
    @PreAuthorize("hasAuthority('category:edit')")
    public boolean update(@PathVariable @NotNull Long id, @RequestBody Category category) {
        category.setId(id);
        categoryService.updateCategory(category);
        return true;
    }

    /**
     * 删除分类
     * 来自 admin/CategoryManagerController
     */
    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('category:delete')")
    public boolean delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return true;
    }

    /**
     * 更新分类状态
     * 来自 admin/CategoryManagerController
     */
    @PostMapping("/update/status/{id}/{status}")
    @PreAuthorize("hasAuthority('category:edit')")
    public void updateStatus(@PathVariable Long id, @PathVariable Boolean status) {
        categoryService.updateCategoryStatus(id, status);
    }
}