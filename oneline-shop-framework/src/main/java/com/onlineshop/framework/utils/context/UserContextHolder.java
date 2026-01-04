package com.onlineshop.framework.utils.context;


import java.util.Objects;

/**
 * 用户上下文工具类
 * 使用 ThreadLocal 存储当前用户信息
 *
 * @author Tomatos
 * @date 2025/12/17
 */
public final class UserContextHolder {

    /**
     * ThreadLocal 用于存储用户信息
     */
    private static final ThreadLocal<UserContext> USER_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 私有构造函数，防止实例化
     */
    private UserContextHolder() {
    }

    /**
     * 获取当前用户ID
     *
     * @return 用户ID
     */
    public static Long getUserId() {
        return Objects.requireNonNull(getUserContext())
                      .getId();
    }

    /**
     * 获取当前用户信息
     *
     * @return 用户信息
     */
    public static UserContext getUserContext() {
        return USER_THREAD_LOCAL.get();
    }

    public static void setUserContext(UserContext userContext) {
        USER_THREAD_LOCAL.set(userContext);
    }

    public static Long getStoreId() {
        return Objects.requireNonNull(getUserContext())
                      .getStoreId();
    }

    public static String getUserRoleCode() {
        return Objects.requireNonNull(getUserContext())
                      .getRoleCode();
    }

    /**
     * 清除当前用户信息
     */
    public static void clear() {
        USER_THREAD_LOCAL.remove();
    }
}