package com.onlineshop.framework.models.statistic.vo;

import com.onlineshop.framework.utils.image.Image;
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
    private Integer rank;
    private Long goodsId;
    private String goodsName;
    private Image goodsMainImage;
    private Integer favoriteTotal;
    private Integer favoriteLast7Days;
}
