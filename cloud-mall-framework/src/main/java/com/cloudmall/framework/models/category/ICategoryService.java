package com.cloudmall.framework.models.category;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudmall.framework.models.category.vo.CategoryNodeVO;

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

    List<CategoryNodeVO> getCategoryTree();

    List<Category> getCategoryList();

    List<CategoryNodeVO> getAllCategoryTree();

    String buildCategoryIdPath(Long categoryId, Long parentId);

    /**
     * 根据分类ID构建分类路径字符串
     * 将从分类路径子节点构建出的分类路径字符串逻辑封装在此
     *
     * @param categoryId 分类ID（叶子节点）
     * @return 分类路径字符串，例如 "1/2/3"
     */
    String buildCategoryPathByLeafCategoryId(Long categoryId);

    /**
     * 按级别查询分类列表
     * 查询指定级别的所有启用分类，按sort排序
     *
     * @param level 分类级别（1为一级，2为二级，以此类推）
     * @return 分类列表，按sort升序排序
     */
    List<Category> getCategoryListByLevel(Integer level);
}
