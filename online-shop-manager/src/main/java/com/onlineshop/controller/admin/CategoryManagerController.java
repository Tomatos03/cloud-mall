package com.onlineshop.controller.admin;

import com.onlineshop.framework.models.category.Category;
import com.onlineshop.framework.models.category.ICategoryService;
import com.onlineshop.framework.models.category.CategoryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类控制器
 *
 * @author Tomatos
 * @date 2025/12/20
 */
@RestController
@RequestMapping("/manager/admin/category")
public class CategoryManagerController {

    @Autowired
    private ICategoryService categoryService;

    /**
     * 获取分类树/列表
     *
     * @return 分类列表
     */
    @GetMapping("/list")
    public List<CategoryVO> list() {
        return categoryService.getCategoryTree();
    }

    /**
     * 添加分类
     *
     * @param category 分类对象
     * @return 是否成功
     */
    @PostMapping("/add")
    public boolean add(@RequestBody Category category) {
        categoryService.addCategory(category);
        return true;
    }

    /**
     * 更新分类
     *
     * @param id       分类ID
     * @param category 分类对象（不含ID）
     * @return 是否成功
     */
    @PostMapping("/update/{id}")
    public boolean update(@PathVariable Long id, @RequestBody Category category) {
        category.setId(id);
        categoryService.updateCategory(category);
        return true;
    }

    /**
     * 删除分类
     *
     * @param id 分类ID
     * @return 是否成功
     */
    @PostMapping("/delete/{id}")
    public boolean delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return true;
    }

    @PostMapping("/update/status/{id}/{status}")
    public void updateStatus(@PathVariable Long id, @PathVariable Boolean status) {
        categoryService.updateCategoryStatus(id, status);
    }

    @GetMapping("/tree")
    public  List<CategoryVO> getCategoryTree() {
        return categoryService.getCategoryTree();
    }
}
