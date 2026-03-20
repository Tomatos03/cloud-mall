package com.onlineshop.framework.mq.consumer.goods;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import com.onlineshop.framework.event.MQTag;
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
    void onMessage_shouldSyncGoodsIndex_whenTagIsSync() {
        IGoodsEsService goodsEsService = mock(IGoodsEsService.class);
        IGoodsService goodsService = mock(IGoodsService.class);

        Goods goods = new Goods();
        goods.setId(1001L);
        goods.setCategoryIdPath("1/2/3");
        goods.setCreateTime(LocalDateTime.now());
        when(goodsService.getById(1001L)).thenReturn(goods);

        GoodsEsIndexConsumer consumer = new GoodsEsIndexConsumer(goodsEsService, goodsService);
        Message<Long> message = buildMessage(MQTag.GOODS_SYNC_TO_ES, 1001L);

        assertDoesNotThrow(() -> consumer.onMessage(message));
        verify(goodsService).getById(1001L);
        verify(goodsEsService).saveGoodsIndex(any());
        verify(goodsEsService, never()).deleteGoodsIndex(any());
    }

    @Test
    void onMessage_shouldDeleteGoodsIndex_whenTagIsDelete() {
        IGoodsEsService goodsEsService = mock(IGoodsEsService.class);
        IGoodsService goodsService = mock(IGoodsService.class);
        GoodsEsIndexConsumer consumer = new GoodsEsIndexConsumer(goodsEsService, goodsService);
        Message<Long> message = buildMessage(MQTag.GOODS_DELETE_FROM_ES, 1002L);

        assertDoesNotThrow(() -> consumer.onMessage(message));
        verify(goodsService, never()).getById(any());
        verify(goodsEsService).deleteGoodsIndex(1002L);
        verify(goodsEsService, never()).saveGoodsIndex(any());
    }

    @Test
    void onMessage_shouldIgnoreWhenTagUnknown() {
        IGoodsEsService goodsEsService = mock(IGoodsEsService.class);
        IGoodsService goodsService = mock(IGoodsService.class);
        GoodsEsIndexConsumer consumer = new GoodsEsIndexConsumer(goodsEsService, goodsService);
        Message<Long> message = buildMessage("unknown_tag", 1003L);

        assertDoesNotThrow(() -> consumer.onMessage(message));
        verify(goodsService, never()).getById(any());
        verify(goodsEsService, never()).saveGoodsIndex(any());
        verify(goodsEsService, never()).deleteGoodsIndex(any());
    }

    private Message<Long> buildMessage(String tag, Long goodsId) {
        return MessageBuilder.withPayload(goodsId)
                             .setHeader(RocketMQHeaders.TAGS, tag)
                             .build();
    }
}
