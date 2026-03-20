package com.onlineshop.framework.mq.consumer.goods;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.onlineshop.framework.models.goods.spu.Goods;
import com.onlineshop.framework.models.goods.spu.IGoodsService;
import com.onlineshop.framework.models.search.service.IGoodsEsService;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GoodsSyncToEsConsumerTest {

    @Test
    void onMessage_shouldSyncGoodsIndex_whenGoodsExists() {
        IGoodsEsService goodsEsService = mock(IGoodsEsService.class);
        IGoodsService goodsService = mock(IGoodsService.class);

        Goods goods = new Goods();
        goods.setId(1001L);
        goods.setCategoryIdPath("1/2/3");
        goods.setCreateTime(LocalDateTime.now());
        when(goodsService.getById(1001L)).thenReturn(goods);

        GoodsSyncToEsConsumer consumer = new GoodsSyncToEsConsumer(goodsEsService, goodsService);

        assertDoesNotThrow(() -> consumer.onMessage(1001L));
        verify(goodsService).getById(1001L);
        verify(goodsEsService).saveGoodsIndex(any());
    }

    @Test
    void onMessage_shouldIgnore_whenGoodsNotExists() {
        IGoodsEsService goodsEsService = mock(IGoodsEsService.class);
        IGoodsService goodsService = mock(IGoodsService.class);
        when(goodsService.getById(1002L)).thenReturn(null);

        GoodsSyncToEsConsumer consumer = new GoodsSyncToEsConsumer(goodsEsService, goodsService);

        assertDoesNotThrow(() -> consumer.onMessage(1002L));
        verify(goodsService).getById(1002L);
        verify(goodsEsService, never()).saveGoodsIndex(any());
    }

    @Test
    void onMessage_shouldIgnore_whenGoodsIdIsNull() {
        IGoodsEsService goodsEsService = mock(IGoodsEsService.class);
        IGoodsService goodsService = mock(IGoodsService.class);
        GoodsSyncToEsConsumer consumer = new GoodsSyncToEsConsumer(goodsEsService, goodsService);

        assertDoesNotThrow(() -> consumer.onMessage(null));
        verify(goodsService, never()).getById(any());
        verify(goodsEsService, never()).saveGoodsIndex(any());
    }
}
