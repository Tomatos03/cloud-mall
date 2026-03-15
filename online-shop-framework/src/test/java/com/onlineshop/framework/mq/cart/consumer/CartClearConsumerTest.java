package com.onlineshop.framework.mq.cart.consumer;

import java.util.Collections;
import java.util.List;

import com.onlineshop.framework.event.cart.ClearCartEvent;
import com.onlineshop.framework.models.cart.ICartService;
import com.onlineshop.framework.mq.consumer.cart.CartClearConsumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class CartClearConsumerTest {

    @Mock
    private ICartService cartService;

    @InjectMocks
    private CartClearConsumer consumer;

    @Test
    void shouldRemoveCartItemsWhenMessageValid() {
        List<Long> skuIds = List.of(101L, 102L);
        ClearCartEvent message = ClearCartEvent.builder()
                                               .userId(1L)
                                               .skuIds(skuIds)
                                               .build();

        consumer.onMessage(message);

        verify(cartService).removeCartItems(1L, skuIds);
    }

    @Test
    void shouldIgnoreWhenMessageInvalid() {
        consumer.onMessage(null);
        consumer.onMessage(ClearCartEvent.builder().userId(null).skuIds(List.of(1L)).build());
        consumer.onMessage(ClearCartEvent.builder().userId(1L).skuIds(Collections.emptyList()).build());

        verifyNoInteractions(cartService);
    }
}
