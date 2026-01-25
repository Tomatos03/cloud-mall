package com.onlineshop.framework.models.category;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineshop.framework.enums.BizErrorCode;
import com.onlineshop.framework.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 商品分类服务实现类
 *
 * @author Tomatos
 * @date 2025/12/20
 */
@Service
public class CategoryService extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {
    private final static Long TREE_ROOT = 0L;
    private final static Integer MAX_LEVEL = 3;

    @Autowired
    private CategoryMapper categoryMapper;

    @CacheEvict(value = "category", allEntries = true)
    @Override
    public void addCategory(Category dto) {
        checkAndFill(dto);
        save(dto);
        syncParentStatusIfChildrenHasEnable(dto);
    }

    @CacheEvict(value = "category", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateCategory(Category dto) {
        categoryMapper.updateById(dto);
        syncParentStatusIfChildrenHasEnable(dto);
        syncChildrenStatus(dto);
    }

    @CacheEvict(value = "category", allEntries = true)
    @Override
    public void deleteCategory(Long categoryId) {
        removeById(categoryId);
        List<Category> childrenCategory = getChildrenCategory(categoryId);
        for (Category category : childrenCategory) {
            deleteCategory(category.getId());
        }
    }

    @CacheEvict(value = "category", allEntries = true)
    @Override
    public void updateCategoryStatus(Long id, Boolean status) {
        Category category = new Category();
        category.setId(id);
        category.setStatus(status);
        updateById(category);
    }

    @Cacheable(value = "category", key = "'tree'")
    @Override
    public List<CategoryVO> getCategoryTree() {
        List<Category> categoryList = getCategoryList();
        return buildChildrenTree(categoryList, TREE_ROOT);
    }

    @Cacheable(value = "category", key = "'list'")
    @Override
    public List<Category> getCategoryList() {
        return lambdaQuery().eq(Category::getStatus, true)
                            .list();
    }

    @Cacheable(value = "category", key = "'allTree'")
    @Override
    public List<CategoryVO> getAllCategoryTree() {
        return buildChildrenTree(list(), TREE_ROOT);
    }

    @Override
    public String getCategoryIdPath(Long categoryId, Long parentId) {
        if (parentId == null || parentId.equals(0L)) {
            return String.valueOf(categoryId);
        } else {
            Category parentCategory = getById(parentId);
            if (parentCategory == null) {
                throw new BusinessException(BizErrorCode.CATEGORY_NOT_EXIST);
            }

            String parentPath = getCategoryIdPath(parentId, parentCategory.getParentId());
            return parentPath + "/" + categoryId;
        }
    }

    private List<CategoryVO> buildChildrenTree(List<Category> categories, Long parent) {
        return categories.stream()
                         .filter(category -> Objects.equals(category.getParentId(), parent))
                         .map(category -> {
                             CategoryVO vo = BeanUtil.copyProperties(category, CategoryVO.class);
                             vo.setChildren(buildChildrenTree(categories, category.getId()));
                             return vo;
                         })
                         .sorted(Comparator.comparingInt(CategoryVO::getSort))
                         .collect(Collectors.toList());
    }

    private void checkAndFill(Category category) {
        Objects.requireNonNull(category.getLevel());
        if (category.getLevel() > MAX_LEVEL) {
            throw new BusinessException(BizErrorCode.CATEGORY_BEYOND_MAX_LEVEL);
        }
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }
    }

    private List<Category> getChildrenCategory(Long categoryId) {
        return list(new QueryWrapper<Category>().eq("parent_id", categoryId));
    }

    private boolean hasEnableStatus(List<Category> categories) {
        for (Category category : categories) {
            if (Boolean.TRUE.equals(category.getStatus())) {
                return true;
            }
        }
        return false;
    }

    private boolean isTopLevelCategory(Category category) {
        return category.getParentId() == null || category.getParentId()
                                                         .equals(0L);
    }

    private void syncChildrenStatus(Category category) {
        if (category.getLevel() == null || category.getLevel() >= MAX_LEVEL) {
            return;
        }
        List<Category> childrenCategory = getChildrenCategory(category.getId());
        for (Category children : childrenCategory) {
            children.setStatus(category.getStatus());
            syncChildrenStatus(children);
        }
        if (!childrenCategory.isEmpty()) {
            // TODO 事务失效待处理
            updateBatchById(childrenCategory);
        }
    }

    private void syncParentStatusIfChildrenHasEnable(Category category) {
        if (isTopLevelCategory(category)) {
            return;
        }
        List<Category> children = getChildrenCategory(category.getParentId());
        if (!children.isEmpty()) {
            Category parent = getById(category.getParentId());
            boolean hasEnable = hasEnableStatus(children);
            parent.setStatus(hasEnable);
            updateById(parent);
            syncParentStatusIfChildrenHasEnable(parent);
        }
    }
}
