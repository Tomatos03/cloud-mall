package com.cloudmall.framework.common.aspect.desensitize;

import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Collection;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/1/29
 */
@Slf4j
@Aspect
@Component
public class DesensitizeAspect {
    @Before("@annotation(com.cloudmall.framework.common.aspect.desensitize.Desensitize)")
    public void doDesensitize(JoinPoint point) {
        Object[] args = point.getArgs();
        for (Object arg : args) {
            processDesensitize(arg);
        }
    }

    private void processDesensitize(Object obj) {
        if (obj == null) {
            return;
        }

        if (obj instanceof Collection<?> collection) {
            for (Object item : collection) {
                processDesensitizeFields(item);
            }
        } else {
            processDesensitizeFields(obj);
        }
    }

    private void processDesensitizeFields(Object obj) {
        if (obj == null) {
            return;
        }

        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            Desensitize annotation = field.getAnnotation(Desensitize.class);
            if (annotation != null) {
                applyDesensitize(obj, field, annotation);
            }
        }
    }

    private void applyDesensitize(Object obj, Field field, Desensitize annotation) {
        try {
            field.setAccessible(true);
            Object value = field.get(obj);
            if (value instanceof String) {
                String desensitized = desensitizeString((String) value, annotation.type());
                field.set(obj, desensitized);
            }
        } catch (IllegalAccessException e) {
            log.error("数据脱敏失败", e);
        }
    }

    private String desensitizeString(String value, DesensitizeType type) {
        if (StrUtil.isBlank(value)) {
            return value;
        }

        return switch (type) {
            case PHONE -> DesensitizedUtil.mobilePhone(value);
            case EMAIL -> DesensitizedUtil.email(value);
            case ID_CARD -> DesensitizedUtil.idCardNum(value, 6, 4);
            default -> value;
        };
    }
}