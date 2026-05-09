package com.cloudmall.framework.models.statistic.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品收藏排行视图对象
 *
 * @author Tomatos
 * @date 2025/12/24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteGoodsTopVO {
    private Long goodsId;
    private String goodsName;
    private String goodsMainImageUrl;
    private Long favoriteTotal;
}