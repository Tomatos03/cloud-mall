package com.cloudmall.framework.models.goods.spu.vo;

import com.cloudmall.framework.models.goods.spu.Goods;
import com.cloudmall.framework.utils.image.ImageUtil;
import com.cloudmall.framework.utils.money.Money;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class GoodsVO {
    private Long id;
    private String name;
    private String sellPoint;
    private String minPrice;
    private String mainImageUrl;

    public static GoodsVO convertToGoodsVO(Goods goods) {
        return GoodsVO.builder()
                      .name(goods.getName())
                      .minPrice(Money.ofCents(goods.getMinPrice()).toYuanString())
                      .id(goods.getId())
                      .sellPoint(goods.getSellPoint())
                      .mainImageUrl(ImageUtil.getMainImageUrl(goods.getDisplayImages()))
                      .build();
    }
}
