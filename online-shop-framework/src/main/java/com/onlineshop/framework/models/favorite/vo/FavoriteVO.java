package com.onlineshop.framework.models.favorite.vo;

import com.onlineshop.framework.models.favorite.Favorite;
import com.onlineshop.framework.utils.money.Money;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 收藏视图对象
 * 用于返回给前端的收藏信息视图
 *
 * @author : Tomatos
 * @date : 2026/1/24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteVO implements Serializable {
    /**
     * 收藏ID
     */
    private Long favoriteId;

    /**
     * 商品ID
     */
    private Long goodsId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品主图URL
     */
    private String goodsMainImageUrl;

    private String goodsPrice;

    /**
     * 商品卖点描述
     */
    private String goodsSellPoint;

    /**
     * 将Favorite实体转换为FavoriteVO
     *
     * @param favorite 收藏实体
     * @return 收藏视图对象
     */
    public static FavoriteVO convertFavoriteVO(Favorite favorite) {
        if (favorite == null) {
            return null;
        }
        return FavoriteVO.builder()
                         .favoriteId(favorite.getId())
                         .goodsId(favorite.getGoodsId())
                         .goodsName(favorite.getGoodsName())
                         .goodsMainImageUrl(favorite.getGoodsMainImageUrl())
                         .goodsPrice(Money.ofCents(favorite.getGoodsPrice()).toYuanString())
                         .goodsSellPoint(favorite.getGoodsSellPoint())
                         .build();
    }
}
