package com.onlineshop.framework.mq.goods.consumer;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.onlineshop.framework.models.goods.spu.Goods;
import com.onlineshop.framework.models.search.service.IGoodsEsService;
import com.onlineshop.framework.mq.consumer.goods.GoodsSyncToEsConsumer;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class GoodsSyncToEsConsumerTest {
    @Mock
    private IGoodsEsService goodsEsService;

    @InjectMocks
    private GoodsSyncToEsConsumer consumer;

    @Test
    void shouldSaveGoodsIndexWhenMessageValid() {
        Goods message = Goods.builder()
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

//        consumer.onMessage(message);

        verify(goodsEsService).saveGoodsIndex(argThat(goodsIndex ->
                goodsIndex != null
                        && Long.valueOf(1L).equals(goodsIndex.getId())
                        && "iPhone".equals(goodsIndex.getName())
                        && Long.valueOf(11L).equals(goodsIndex.getStoreId())
        ));
    }

    @Test
    void shouldIgnoreWhenMessageInvalid() {
        consumer.onMessage(null);
//        consumer.onMessage(Goods.builder().id(null).build());

        verifyNoInteractions(goodsEsService);
    }
}
