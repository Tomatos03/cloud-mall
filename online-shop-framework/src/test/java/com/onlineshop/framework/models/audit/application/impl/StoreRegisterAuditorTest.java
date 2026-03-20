package com.onlineshop.framework.models.audit.application.impl;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.exception.BizException;
import com.onlineshop.framework.models.audit.dto.AuditSubmitDTO;
import com.onlineshop.framework.models.audit.dto.StoreRegisterAuditItemDTO;
import com.onlineshop.framework.models.auth.enums.AccountType;
import com.onlineshop.framework.models.store.IStoreService;
import com.onlineshop.framework.models.system.user.IUserService;
import com.onlineshop.framework.models.system.user.entity.User;
import com.onlineshop.framework.models.system.user.mapper.UserQualificationMapper;
import com.onlineshop.framework.security.AuthUser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StoreRegisterAuditorTest {

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validateAndFill_shouldFillCurrentUserIdForAllItems() {
        IUserService userService = mock(IUserService.class);
        when(userService.getById(9527L)).thenReturn(User.builder().id(9527L).build());
        StoreRegisterAuditor auditor = new StoreRegisterAuditor(
                mock(IStoreService.class),
                mock(UserQualificationMapper.class),
                userService
        );
        mockAuthUser(9527L, AccountType.NORMAL);

        StoreRegisterAuditItemDTO item1 = new StoreRegisterAuditItemDTO();
        StoreRegisterAuditItemDTO item2 = new StoreRegisterAuditItemDTO();
        AuditSubmitDTO<StoreRegisterAuditItemDTO> submitDTO =
                AuditSubmitDTO.of("STORE_REGISTER", Arrays.asList(item1, item2));

        ReflectionTestUtils.invokeMethod(auditor, "validateAndFill", submitDTO);

        assertEquals(9527L, item1.getUserId());
        assertEquals(9527L, item2.getUserId());
    }

    @Test
    void validateAndFill_shouldThrowWhenCurrentUserIsMerchant() {
        IUserService userService = mock(IUserService.class);
        when(userService.getById(1000L)).thenReturn(User.builder().id(1000L).build());
        StoreRegisterAuditor auditor = new StoreRegisterAuditor(
                mock(IStoreService.class),
                mock(UserQualificationMapper.class),
                userService
        );
        mockAuthUser(1000L, AccountType.MERCHANT);

        StoreRegisterAuditItemDTO item = new StoreRegisterAuditItemDTO();
        AuditSubmitDTO<StoreRegisterAuditItemDTO> submitDTO =
                AuditSubmitDTO.of("STORE_REGISTER", Collections.singletonList(item));

        BizException exception = assertThrows(
                BizException.class,
                () -> ReflectionTestUtils.invokeMethod(auditor, "validateAndFill", submitDTO)
        );

        assertEquals(BizErrorCode.NO_PERMISSION, exception.getBizErrorCode());
    }

    @Test
    void validateAndFill_shouldThrowWhenCurrentUserNotExists() {
        IUserService userService = mock(IUserService.class);
        when(userService.getById(2000L)).thenReturn(null);
        StoreRegisterAuditor auditor = new StoreRegisterAuditor(
                mock(IStoreService.class),
                mock(UserQualificationMapper.class),
                userService
        );
        mockAuthUser(2000L, AccountType.NORMAL);

        StoreRegisterAuditItemDTO item = new StoreRegisterAuditItemDTO();
        AuditSubmitDTO<StoreRegisterAuditItemDTO> submitDTO =
                AuditSubmitDTO.of("STORE_REGISTER", Collections.singletonList(item));

        BizException exception = assertThrows(
                BizException.class,
                () -> ReflectionTestUtils.invokeMethod(auditor, "validateAndFill", submitDTO)
        );

        assertEquals(BizErrorCode.USER_NOT_EXISTS, exception.getBizErrorCode());
    }

    private void mockAuthUser(Long userId, AccountType accountType) {
        AuthUser authUser = new AuthUser(userId, "user" + userId, "password", Collections.emptyList());
        authUser.setCurrentAccountType(accountType.getCode());

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authenticationToken);
        SecurityContextHolder.setContext(context);
    }
}
