package com.cloudmall.framework.utils;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.cloudmall.framework.common.enums.BizErrorCode;
import com.cloudmall.framework.models.auth.enums.AccountType;
import com.cloudmall.framework.security.AuthUser;

/**
 * SecurityUserUtils
 * 便捷获取和设置认证用户信息
 *
 * @author Tomatos
 * @date 2026/02/02
 */
public final class AuthUserUtils {

    private AuthUserUtils() {
    }

    /**
     * 获取当前认证对象
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 获取当前认证用户
     */
    public static AuthUser getAuthUser() {
        Authentication authentication = getAuthentication();
        AssertUtils.notNull(authentication, BizErrorCode.USER_NOT_AUTHENTICATED);

        Object principal = authentication.getPrincipal();
        AssertUtils.isTrue(principal instanceof UserDetails, BizErrorCode.USER_NOT_AUTHENTICATED);
        return (AuthUser) principal;
    }

    /**
     * 获取当前用户ID
     */
    public static Long getUserId() {
        return getAuthUser().getUserId();
    }

    /**
     * 获取当前用户名
     */
    public static String getUsername() {
        return getAuthUser().getUsername();
    }

    /**
     * 获取当前用户角色
     */
    public static List<String> getRoles() {
        return getAuthUser().getAuthorities()
                            .stream()
                            .map(GrantedAuthority::getAuthority)
                            .toList();
    }

    /**
     * 获取当前用户店铺ID
     */
    public static Long getStoreId() {
        return getAuthUser().getStoreId();
    }

    /**
     * 获取当前请求账号类型编码
     */
    public static String getAccountTypeCode() {
        String accountTypeCode = getAuthUser().getCurrentAccountType();
        AssertUtils.assertNotBlank(accountTypeCode, BizErrorCode.INVALID_CLIENT_TYPE);
        return accountTypeCode;
    }

    /**
     * 获取当前请求账号类型
     */
    public static AccountType getAccountType() {
        return AccountType.of(getAccountTypeCode());
    }

    /**
     * 当前请求是否来自普通用户端
     */
    public static boolean isNormalAccount() {
        return getAccountType().isNormal();
    }

    /**
     * 当前请求是否来自商家端
     */
    public static boolean isMerchantAccount() {
        return getAccountType().isMerchant();
    }

    /**
     * 当前请求是否来自管理端
     */
    public static boolean isAdminAccount() {
        return getAccountType().isAdmin();
    }

    /**
     * 设置当前认证对象
     */
    public static void setAuthentication(Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * 清除当前认证信息
     */
    public static void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }
}
