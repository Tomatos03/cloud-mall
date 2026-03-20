package com.onlineshop.framework.models.order.wrapper;

import java.util.Collections;
import java.util.Map;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.onlineshop.framework.models.auth.enums.AccountType;
import com.onlineshop.framework.models.order.dto.OrderParamsDTO;
import com.onlineshop.framework.models.order.entity.Order;
import com.onlineshop.framework.models.order.enums.OrderType;
import com.onlineshop.framework.security.AuthUser;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderQueryWrapperTest {

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void build_shouldRestrictAdminOrdersToParentAndNormal() {
        initOrderTableInfo();
        mockAuthUser(1L, null, AccountType.ADMIN);

        LambdaQueryWrapper<Order> wrapper = OrderQueryWrapper.build(new OrderParamsDTO());
        wrapper.getSqlSegment();
        Map<String, Object> params = wrapper.getParamNameValuePairs();

        assertTrue(params.containsValue(OrderType.PARENT.getCode()));
        assertTrue(params.containsValue(OrderType.NORMAL.getCode()));
        assertFalse(params.containsValue(OrderType.SUB.getCode()));
    }

    @Test
    void build_shouldRestrictMerchantOrdersToSubAndNormal() {
        initOrderTableInfo();
        mockAuthUser(2L, 100L, AccountType.MERCHANT);

        LambdaQueryWrapper<Order> wrapper = OrderQueryWrapper.build(new OrderParamsDTO());
        wrapper.getSqlSegment();
        Map<String, Object> params = wrapper.getParamNameValuePairs();

        assertTrue(params.containsValue(100L));
        assertTrue(params.containsValue(OrderType.SUB.getCode()));
        assertTrue(params.containsValue(OrderType.NORMAL.getCode()));
        assertFalse(params.containsValue(OrderType.PARENT.getCode()));
    }

    private void mockAuthUser(Long userId, Long storeId, AccountType accountType) {
        AuthUser authUser = new AuthUser(userId, "user" + userId, "password", Collections.emptyList());
        authUser.setStoreId(storeId);
        authUser.setCurrentAccountType(accountType.getCode());

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authenticationToken);
        SecurityContextHolder.setContext(context);
    }

    private void initOrderTableInfo() {
        MapperBuilderAssistant builderAssistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(builderAssistant, Order.class);
    }
}
