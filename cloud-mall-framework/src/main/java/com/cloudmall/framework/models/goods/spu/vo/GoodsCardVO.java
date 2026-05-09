package com.cloudmall.framework.models.goods.spu.vo;

import com.cloudmall.framework.models.goods.spu.Goods;
import com.cloudmall.framework.models.search.index.GoodsIndex;
import com.cloudmall.framework.utils.image.ImageUtil;
import com.cloudmall.framework.utils.money.Money;
import lombok.*;

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
     * 将Goods对象转换为GoodsCardVO
     *
     * @param goods Goods对象
     * @return GoodsCardVO对象
     */
    public static GoodsCardVO convertGoodsCardVO(@NonNull Goods goods) {
        return GoodsCardVO.builder()
                          .id(goods.getId())
                          .name(goods.getName())
                          .mainImageUrl(ImageUtil.getMainImageUrl(goods.getDisplayImages()))
                          .minPrice(Money.ofCents(goods.getMinPrice()).toYuanString())
                          .sellPoint(goods.getSellPoint())
                          .sale(goods.getSales())
                          .build();
    }

    public static GoodsCardVO convertGoodsCardVO(@NonNull GoodsIndex goodsIndex) {
        return GoodsCardVO.builder()
                          .id(goodsIndex.getId())
                          .name(goodsIndex.getName())
                          .mainImageUrl(ImageUtil.getMainImageUrl(goodsIndex.getDisplayImages()))
                          .minPrice(Money.ofCents(goodsIndex.getMinPrice()).toYuanString())
                          .sellPoint(goodsIndex.getSellPoint())
                          .sale(goodsIndex.getSales())
                          .build();
    }
}