package com.onlineshop.framework.models.category;

import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 商品分类服务接口
 *
 * @author Tomatos
 * @date 2025/12/20
 */
public interface ICategoryService extends IService<Category> {
    void addCategory(Category dto);

    void updateCategory(Category dto);

    void deleteCategory(Long categoryId);

    void updateCategoryStatus(Long id, Boolean status);

    List<CategoryVO> getCategoryTree();

    List<Category> getCategoryList();

    List<CategoryVO> getAllCategoryTree();

    String getCategoryIdPath(Long categoryId, Long parentId);
}
