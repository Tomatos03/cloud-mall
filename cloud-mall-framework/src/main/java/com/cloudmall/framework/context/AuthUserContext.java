package com.cloudmall.framework.context;

import java.util.List;

import com.cloudmall.framework.common.enums.BizErrorCode;
import com.cloudmall.framework.models.auth.enums.AccountType;
import com.cloudmall.framework.security.AuthUser;
import com.cloudmall.framework.utils.AssertUtils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public final class AuthUserContext {

    private AuthUserContext() {
    }

    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public static AuthUser getAuthUser() {
        Authentication authentication = getAuthentication();
        AssertUtils.notNull(authentication, BizErrorCode.USER_NOT_AUTHENTICATED);

        Object principal = authentication.getPrincipal();
        AssertUtils.isTrue(principal instanceof UserDetails, BizErrorCode.USER_NOT_AUTHENTICATED);
        return (AuthUser) principal;
    }

    public static Long getUserId() {
        return getAuthUser().getUserId();
    }

    public static String getUsername() {
        return getAuthUser().getUsername();
    }

    public static List<String> getRoles() {
        return getAuthUser().getAuthorities()
                            .stream()
                            .map(GrantedAuthority::getAuthority)
                            .toList();
    }

    public static Long getStoreId() {
        return getAuthUser().getStoreId();
    }

    public static String getAccountTypeCode() {
        String accountTypeCode = getAuthUser().getCurrentAccountType();
        AssertUtils.assertNotBlank(accountTypeCode, BizErrorCode.INVALID_CLIENT_TYPE);
        return accountTypeCode;
    }

    public static AccountType getAccountType() {
        return AccountType.of(getAccountTypeCode());
    }

    public static boolean isNormalAccount() {
        return getAccountType().isNormal();
    }

    public static boolean isMerchantAccount() {
        return getAccountType().isMerchant();
    }

    public static boolean isAdminAccount() {
        return getAccountType().isAdmin();
    }

    public static void setAuthentication(Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public static void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }
}
