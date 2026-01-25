package com.onlineshop.framework.models.goods.spu.vo;

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
}
