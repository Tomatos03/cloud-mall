package com.cloudmall.framework.common.aspect.ratelimit;

import java.util.concurrent.TimeUnit;

import com.cloudmall.framework.utils.AssertUtils;
import com.cloudmall.framework.context.AuthUserContext;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 接口限流切面
 */
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {
    private final RedisTemplate<String, Object> redisTemplate;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = rateLimit.keyPrefix() + AuthUserContext.getUserId();
        Long count = redisTemplate.opsForValue().increment(key);
        AssertUtils.notNull(count, rateLimit.errorCode());

        if (count == 1) {
            redisTemplate.expire(key, rateLimit.periodSeconds(), TimeUnit.SECONDS);
        }

        AssertUtils.isTrue(count <= rateLimit.count(), rateLimit.errorCode());
        return joinPoint.proceed();
    }
}
