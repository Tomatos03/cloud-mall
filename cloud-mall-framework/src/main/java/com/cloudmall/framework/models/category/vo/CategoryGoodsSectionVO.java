package com.cloudmall.framework.models.category.vo;

import com.cloudmall.framework.models.goods.spu.vo.GoodsCardVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 分类商品区域VO
 * 展示一级分类及其下的二级分类和商品
 *
 * @author Tomatos
 * @date 2026/3/2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryGoodsSectionVO {
    /**
     * 一级分类信息
     */
    private CategoryTabVO category;

    /**
     * 该一级分类下的所有二级分类列表
     */
    private List<CategoryTabVO> tabs;

    /**
     * 所有二级分类的商品映射
     * key: 二级分类ID
     * value: 该二级分类下的商品列表（最多8条，按销量排序）
     */
    private Map<Long, List<GoodsCardVO>> goodsMap;
}