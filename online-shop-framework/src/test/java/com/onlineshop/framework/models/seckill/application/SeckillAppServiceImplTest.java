package com.onlineshop.framework.models.seckill.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.exception.BizException;
import com.onlineshop.framework.models.seckill.application.vo.SeckillParticipateResultVO;
import com.onlineshop.framework.models.seckill.entity.SeckillOrder;
import com.onlineshop.framework.models.seckill.service.SeckillActivityService;
import com.onlineshop.framework.models.seckill.service.SeckillGoodsService;
import com.onlineshop.framework.models.seckill.service.SeckillOrderService;
import com.onlineshop.framework.mq.seckill.producer.SeckillOrderProducer;
import com.onlineshop.framework.security.AuthUser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeckillAppServiceImplTest {

    @Mock
    private SeckillActivityService seckillActivityService;
    @Mock
    private SeckillGoodsService seckillGoodsService;
    @Mock
    private SeckillOrderService seckillOrderService;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ValueOperations<String, Object> valueOperations;
    @Mock
    private SeckillOrderProducer seckillOrderProducer;

    private SeckillAppServiceImpl seckillAppService;

    @BeforeEach
    void setUp() {
        seckillAppService = new SeckillAppServiceImpl();
        ReflectionTestUtils.setField(seckillAppService, "seckillActivityService", seckillActivityService);
        ReflectionTestUtils.setField(seckillAppService, "seckillGoodsService", seckillGoodsService);
        ReflectionTestUtils.setField(seckillAppService, "seckillOrderService", seckillOrderService);
        ReflectionTestUtils.setField(seckillAppService, "redisTemplate", redisTemplate);
        ReflectionTestUtils.setField(seckillAppService, "seckillOrderCreateProducer", seckillOrderProducer);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void participateSeckill_shouldSavePendingSeckillOrderAndSendMessageWhenStockDeducted() {
        Long seckillGoodsId = 100L;
        Long userId = 200L;
        Integer quantity = 1;
        LocalDateTime now = LocalDateTime.now();
        int startHour = now.getHour() == 23 ? 22 : now.getHour();

        String goodsCacheJson = buildGoodsCacheJson(seckillGoodsId, BigDecimal.valueOf(9.90), startHour);
        when(valueOperations.get("seckill:goods:" + seckillGoodsId)).thenReturn(goodsCacheJson);
        when(redisTemplate.execute(any(), anyList(), eq(quantity), any())).thenReturn(18L);
        when(seckillOrderService.save(any(SeckillOrder.class))).thenAnswer(invocation -> {
            SeckillOrder order = invocation.getArgument(0);
            order.setId(9527L);
            return true;
        });

        mockCurrentUser(userId);
        SeckillParticipateResultVO result = seckillAppService.participateSeckill(seckillGoodsId, quantity);

        ArgumentCaptor<SeckillOrder> orderCaptor = ArgumentCaptor.forClass(SeckillOrder.class);
        verify(seckillOrderService).save(orderCaptor.capture());
        SeckillOrder savedOrder = orderCaptor.getValue();

        assertEquals(userId, savedOrder.getUserId());
        assertEquals(seckillGoodsId, savedOrder.getGoodsId());
        assertEquals(quantity, savedOrder.getQuantity());
        assertEquals(BigDecimal.valueOf(9.90), savedOrder.getPrice());
        assertEquals(0, savedOrder.getStatus());
        assertNotNull(savedOrder.getOrderNo());
        verify(seckillOrderProducer).sendSeckillOrderCreate(eq(9527L));

        assertTrue(result.isSuccess());
        assertEquals("秒杀请求已受理，订单正在创建中", result.getMessage());
        assertEquals(9527L, result.getOrderId());
        assertEquals(18, result.getRemainingStock());
        verify(seckillGoodsService, never()).getById(any());
        verify(seckillActivityService, never()).getById(any());
    }

    @Test
    void participateSeckill_shouldThrowWhenStockInsufficient() {
        Long seckillGoodsId = 101L;
        Long userId = 201L;
        Integer quantity = 2;
        LocalDateTime now = LocalDateTime.now();
        int startHour = now.getHour() == 23 ? 22 : now.getHour();

        String goodsCacheJson = buildGoodsCacheJson(seckillGoodsId, BigDecimal.valueOf(10), startHour);
        when(valueOperations.get("seckill:goods:" + seckillGoodsId)).thenReturn(goodsCacheJson);
        when(redisTemplate.execute(any(), anyList(), eq(quantity), any())).thenReturn(-1L);

        mockCurrentUser(userId);
        BizException exception = assertThrows(BizException.class,
                                              () -> seckillAppService.participateSeckill(seckillGoodsId, quantity));
        assertEquals(BizErrorCode.SECKILL_STOCK_INSUFFICIENT, exception.getBizErrorCode());
        verify(seckillOrderService, never()).save(any());
        verify(seckillOrderProducer, never()).sendSeckillOrderCreate(any(Long.class));
        verify(seckillGoodsService, never()).getById(any());
        verify(seckillActivityService, never()).getById(any());
    }

    @Test
    void participateSeckill_shouldThrowWhenRepeatOrder() {
        Long seckillGoodsId = 102L;
        Long userId = 202L;
        Integer quantity = 1;
        LocalDateTime now = LocalDateTime.now();
        int startHour = now.getHour() == 23 ? 22 : now.getHour();

        String goodsCacheJson = buildGoodsCacheJson(seckillGoodsId, BigDecimal.valueOf(10), startHour);
        when(valueOperations.get("seckill:goods:" + seckillGoodsId)).thenReturn(goodsCacheJson);
        when(redisTemplate.execute(any(), anyList(), eq(quantity), any())).thenReturn(-2L);

        mockCurrentUser(userId);
        BizException exception = assertThrows(BizException.class,
                                              () -> seckillAppService.participateSeckill(seckillGoodsId, quantity));
        assertEquals(BizErrorCode.SECKILL_REPEAT_ORDER, exception.getBizErrorCode());
        verify(seckillOrderService, never()).save(any());
        verify(seckillOrderProducer, never()).sendSeckillOrderCreate(any(Long.class));
        verify(seckillGoodsService, never()).getById(any());
        verify(seckillActivityService, never()).getById(any());
    }

    private String buildGoodsCacheJson(Long seckillGoodsId, BigDecimal seckillPrice, Integer startHour) {
        return "{"
                + "\"id\":" + seckillGoodsId + ","
                + "\"seckillPrice\":" + seckillPrice + ","
                + "\"activityDate\":\"" + LocalDate.now() + "\","
                + "\"startHour\":" + startHour
                + "}";
    }

    private void mockCurrentUser(Long userId) {
        AuthUser authUser = new AuthUser(userId, "user" + userId, "password", Collections.emptyList());
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authenticationToken);
        SecurityContextHolder.setContext(context);
    }
}
