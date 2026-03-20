package com.onlineshop.framework.mq.goods.consumer;

import java.time.LocalDateTime;

import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import com.onlineshop.framework.event.MQTag;
import com.onlineshop.framework.models.goods.spu.Goods;
import com.onlineshop.framework.models.goods.spu.IGoodsService;
import com.onlineshop.framework.models.search.service.IGoodsEsService;
import com.onlineshop.framework.mq.consumer.goods.GoodsEsIndexConsumer;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GoodsSyncToEsConsumerTest {

    @Test
    void shouldSaveGoodsIndexWhenTagIsSync() {
        IGoodsEsService goodsEsService = mock(IGoodsEsService.class);
        IGoodsService goodsService = mock(IGoodsService.class);
        GoodsEsIndexConsumer consumer = new GoodsEsIndexConsumer(goodsEsService, goodsService);

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

        Message<Long> message = buildMessage(MQTag.GOODS_SYNC_TO_ES, 1L);
        consumer.onMessage(message);
        verify(goodsEsService).saveGoodsIndex(argThat(goodsIndex ->
                goodsIndex != null
                        && Long.valueOf(1L).equals(goodsIndex.getId())
                        && "iPhone".equals(goodsIndex.getName())
                        && Long.valueOf(11L).equals(goodsIndex.getStoreId())
        ));
    }

    @Test
    void shouldDeleteGoodsIndexWhenTagIsDelete() {
        IGoodsEsService goodsEsService = mock(IGoodsEsService.class);
        IGoodsService goodsService = mock(IGoodsService.class);
        GoodsEsIndexConsumer consumer = new GoodsEsIndexConsumer(goodsEsService, goodsService);
        Message<Long> message = buildMessage(MQTag.GOODS_DELETE_FROM_ES, 2L);

        consumer.onMessage(message);
        verify(goodsEsService).deleteGoodsIndex(2L);
        verify(goodsService, never()).getById(2L);
    }

    private Message<Long> buildMessage(String tag, Long goodsId) {
        return MessageBuilder.withPayload(goodsId)
                             .setHeader(RocketMQHeaders.TAGS, tag)
                             .build();
    }
}
