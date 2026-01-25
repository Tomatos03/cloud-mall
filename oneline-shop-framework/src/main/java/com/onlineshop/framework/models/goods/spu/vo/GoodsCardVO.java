package com.onlineshop.framework.models.goods.spu.vo;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.onlineshop.framework.enums.SearchOrderType;
import com.onlineshop.framework.models.goods.spu.Goods;
import com.onlineshop.framework.models.goods.spu.dto.GoodsSearchDTO;
import com.onlineshop.framework.utils.image.ImageUtil;
import com.onlineshop.framework.utils.money.Money;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/1/1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsCardVO {
    private Long id;
    private String name;
    private String sellPoint;
    private String mainImageUrl;
    private String minPrice;
    private Integer sale;

    /**
     * 构建搜索商品的QueryWrapper
     *
     * @param leafCategoryIds 叶子分类ID列表（可为null）
     * @param searchDTO       商品搜索DTO对象，包含关键词、排序类型、分页等信息
     * @return QueryWrapper对象
     */
    public static QueryWrapper<Goods> buildSearchWrapper(
            List<Long> leafCategoryIds,
            GoodsSearchDTO searchDTO
    ) {
        QueryWrapper<Goods> queryWrapper = new QueryWrapper<>();

        // 关键词搜索
        String keyword = searchDTO.getKeyword();
        queryWrapper.and(StringUtils.hasText(keyword), wrapper ->
                wrapper.like("name", keyword)
                       .or()
                       .like("info", keyword)
                       .or()
                       .like("description", keyword)
        );

        // 分类过滤
        queryWrapper.in(leafCategoryIds != null && !leafCategoryIds.isEmpty(),
                        "category_id", leafCategoryIds);

        // 状态过滤
        queryWrapper.eq("status", true);

        // 排序
        String sortType = searchDTO.getSortType();
        SearchOrderType orderType = SearchOrderType.of(sortType);
        switch (orderType) {
            case SALES:
                queryWrapper.orderByDesc("sales");
                queryWrapper.orderByDesc("create_date");
                break;
            case COMPREHENSIVE:
            default:
                queryWrapper.orderByDesc("id");
                break;
        }

        return queryWrapper;
    }

    /**
     * 将Goods对象转换为GoodsCardVO
     *
     * @param goods Goods对象
     * @return GoodsCardVO对象
     */
    public static GoodsCardVO convertGoodsCardVO(Goods goods) {
        if (goods == null) {
            return null;
        }

        return GoodsCardVO.builder()
                          .id(goods.getId())
                          .name(goods.getName())
                          .mainImageUrl(ImageUtil.getMainImageUrl(goods.getDisplayImages()))
                          .minPrice(Money.ofCents(goods.getMinPrice()).toYuanString())
                          .sellPoint(goods.getSellPoint())
                          .sale(goods.getSales())
                          .build();
    }
}