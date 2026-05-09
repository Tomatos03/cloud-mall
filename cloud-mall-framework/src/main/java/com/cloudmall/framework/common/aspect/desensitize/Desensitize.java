package com.cloudmall.framework.common.aspect.desensitize;

import java.lang.annotation.*;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/1/29
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Desensitize {
    DesensitizeType type();
}
