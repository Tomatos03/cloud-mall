package com.onlineshop.framework.common.aspect.ratelimit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.onlineshop.framework.common.enums.BizErrorCode;

/**
 * 接口限流注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {
    /**
     * Redis key 前缀
     */
    String keyPrefix() default "rate_limit:";

    /**
     * 时间窗口（秒）
     */
    long periodSeconds() default 60L;

    /**
     * 时间窗口内最大请求次数
     */
    int count() default 10;

    /**
     * 超限错误码
     */
    BizErrorCode errorCode() default BizErrorCode.SECKILL_RATE_LIMIT_EXCEEDED;
}
