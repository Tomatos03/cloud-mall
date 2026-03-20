package com.onlineshop.framework.models.order.application;

import java.time.LocalDateTime;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.onlineshop.framework.models.cart.PurchaseMode;
import com.onlineshop.framework.models.goods.sku.IGoodsSkuService;
import com.onlineshop.framework.models.goods.spu.IGoodsService;
import com.onlineshop.framework.models.order.application.creator.IOrderCreator;
import com.onlineshop.framework.models.order.application.creator.OrderCreatorFactory;
import com.onlineshop.framework.models.order.dto.OrderCreateResultDTO;
import com.onlineshop.framework.models.order.dto.TradeDTO;
import com.onlineshop.framework.models.order.service.IOrderItemService;
import com.onlineshop.framework.models.order.service.IOrderService;
import com.onlineshop.framework.models.store.IStoreService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderAppServiceTest {

    @Test
    void createOrder_shouldFillMockPayQrCodeIntoResult() {
        OrderCreatorFactory orderCreatorFactory = mock(OrderCreatorFactory.class);
        IOrderService orderService = mock(IOrderService.class);
        IOrderItemService orderItemService = mock(IOrderItemService.class);
        IStoreService storeService = mock(IStoreService.class);
        IGoodsService goodsService = mock(IGoodsService.class);
        IGoodsSkuService goodsSkuService = mock(IGoodsSkuService.class);
        OrderAppService orderAppService = new OrderAppService(
                orderCreatorFactory,
                orderService,
                orderItemService,
                storeService,
                goodsService,
                goodsSkuService
        );
        TradeDTO tradeDTO = new TradeDTO();
        PurchaseMode purchaseMode = PurchaseMode.CART_BUY;

        IOrderCreator orderCreator = mock(IOrderCreator.class);
        OrderCreateResultDTO creatorResult = OrderCreateResultDTO.builder()
                                                                 .orderNo("ORDER_1001")
                                                                 .build();
        when(orderCreatorFactory.getOrderCreator(purchaseMode)).thenReturn(orderCreator);
        when(orderCreator.create(tradeDTO)).thenReturn(creatorResult);

        OrderCreateResultDTO result = orderAppService.createOrder(tradeDTO, purchaseMode);

        assertEquals("ORDER_1001", result.getOrderNo());
        assertEquals(OrderCreateResultDTO.MOCK_PAY_QR_CODE, result.getPayQrCode());
    }

    @Test
    void closeTimeoutOrders_shouldQueryOrdersBeforeThirtyMinutesAgo() {
        OrderCreatorFactory orderCreatorFactory = mock(OrderCreatorFactory.class);
        IOrderService orderService = mock(IOrderService.class);
        IOrderItemService orderItemService = mock(IOrderItemService.class);
        IStoreService storeService = mock(IStoreService.class);
        IGoodsService goodsService = mock(IGoodsService.class);
        IGoodsSkuService goodsSkuService = mock(IGoodsSkuService.class);
        OrderAppService orderAppService = new OrderAppService(
                orderCreatorFactory,
                orderService,
                orderItemService,
                storeService,
                goodsService,
                goodsSkuService
        );
        when(orderService.queryTimeoutOrders(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        LocalDateTime expectedEarliest = LocalDateTime.now().minusMinutes(30L);
        orderAppService.closeTimeoutOrders();
        LocalDateTime expectedLatest = LocalDateTime.now().minusMinutes(30L);

        ArgumentCaptor<LocalDateTime> deadlineCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(orderService).queryTimeoutOrders(deadlineCaptor.capture());
        LocalDateTime actualDeadline = deadlineCaptor.getValue();

        assertFalse(actualDeadline.isBefore(expectedEarliest));
        assertFalse(actualDeadline.isAfter(expectedLatest));
    }
}
