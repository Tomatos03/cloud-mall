package com.onlineshop.framework.mq.goods.consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.onlineshop.framework.models.search.service.IGoodsEsService;
import com.onlineshop.framework.mq.consumer.goods.GoodsDeleteFromEsConsumer;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class GoodsDeleteFromEsConsumerTest {
    @Mock
    private IGoodsEsService goodsEsService;

    @InjectMocks
    private GoodsDeleteFromEsConsumer consumer;

    @Test
    void shouldDeleteGoodsIndexWhenMessageValid() {
        consumer.onMessage(1L);

        verify(goodsEsService).deleteGoodsIndex(1L);
    }

    @Test
    void shouldIgnoreWhenMessageInvalid() {
        consumer.onMessage(null);

        verifyNoInteractions(goodsEsService);
    }
}
