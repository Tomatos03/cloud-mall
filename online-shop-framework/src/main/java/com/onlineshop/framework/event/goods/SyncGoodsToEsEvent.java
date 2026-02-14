package com.onlineshop.framework.event.goods;

import com.onlineshop.framework.models.goods.spu.Goods;
import lombok.Builder;
import lombok.Data;

/**
 * 同步商品信息到ES事件
 *
 * @author : Tomatos
 * @date : 2026/1/27
 */
@Data
@Builder
public class SyncGoodsToEsEvent {
    private Goods goods;
}
