package com.onlineshop.framework.utils;

import com.onlineshop.framework.security.AuthUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.utils.AssertUtils;

/**
 * SecurityUserUtils
 * 便捷获取和设置认证用户信息
 *
 * @author Tomatos
 * @date 2026/02/02
 */
public final class AuthUserUtils {

    private AuthUserUtils() {}

    /**
     * 获取当前认证对象
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }


    /**
     * 获取当前认证用户的ParsedToken对象
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

    public static List<String> getRoles() {
        return getAuthUser().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }

    /**
     * 获取当前用户店铺ID
     */
    public static Long getStoreId() {
        //        AssertUtils.notNull(storeId, BizErrorCode.USER_STORE_ID_NULL);
        return getAuthUser().getStoreId();
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