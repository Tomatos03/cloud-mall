package com.onlineshop.framework.event.goods;

import lombok.Builder;
import lombok.Data;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/1/29
 */
@Builder
@Data
public class DelGoodsFromEsEvent {
    private Long goodsId;
}
