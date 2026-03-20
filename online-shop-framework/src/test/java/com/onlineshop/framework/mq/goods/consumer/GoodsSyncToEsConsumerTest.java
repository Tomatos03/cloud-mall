package com.onlineshop.framework.mq.goods.consumer;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.onlineshop.framework.models.goods.spu.Goods;
import com.onlineshop.framework.models.goods.spu.IGoodsService;
import com.onlineshop.framework.models.search.service.IGoodsEsService;
import com.onlineshop.framework.mq.consumer.goods.GoodsDeleteFromEsConsumer;
import com.onlineshop.framework.mq.consumer.goods.GoodsSyncToEsConsumer;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GoodsSyncToEsConsumerTest {

    @Test
    void shouldSaveGoodsIndexWhenSyncConsumerReceivesMessage() {
        IGoodsEsService goodsEsService = mock(IGoodsEsService.class);
        IGoodsService goodsService = mock(IGoodsService.class);
        GoodsSyncToEsConsumer consumer = new GoodsSyncToEsConsumer(goodsEsService, goodsService);

        Goods goods = Goods.builder()
                           .id(1L)
                           .name("iPhone")
                           .sellPoint("A18")
                           .categoryIdPath("1/2/3")
                           .storeId(11L)
                           .storeName("Apple")
                           .minPrice(100L)
                           .maxPrice(200L)
                           .sales(99)
                           .status(Boolean.TRUE)
                           .displayImages("img1")
                           .createTime(LocalDateTime.now())
                           .build();
        when(goodsService.getById(1L)).thenReturn(goods);

        consumer.onMessage(1L);
        verify(goodsEsService).saveGoodsIndex(argThat(goodsIndex ->
                goodsIndex != null
                        && Long.valueOf(1L).equals(goodsIndex.getId())
                        && "iPhone".equals(goodsIndex.getName())
                        && Long.valueOf(11L).equals(goodsIndex.getStoreId())
        ));
    }

    @Test
    void shouldDeleteGoodsIndexWhenDeleteConsumerReceivesMessage() {
        IGoodsEsService goodsEsService = mock(IGoodsEsService.class);
        GoodsDeleteFromEsConsumer consumer = new GoodsDeleteFromEsConsumer(goodsEsService);

        consumer.onMessage(2L);
        verify(goodsEsService).deleteGoodsIndex(2L);
    }

    @Test
    void shouldIgnoreWhenDeleteConsumerReceivesNullGoodsId() {
        IGoodsEsService goodsEsService = mock(IGoodsEsService.class);
        GoodsDeleteFromEsConsumer consumer = new GoodsDeleteFromEsConsumer(goodsEsService);

        consumer.onMessage(null);
        verify(goodsEsService, never()).deleteGoodsIndex(anyLong());
    }
}
