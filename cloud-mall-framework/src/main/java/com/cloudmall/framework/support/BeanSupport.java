package com.cloudmall.framework.support;

import lombok.Getter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Spring Bean获取工具类
 *
 * 提供静态方法便捷获取Spring容器中的Bean。
 *
 * @author : Tomatos
 * @date : 2026/1/10
 */
@Component
public class BeanSupport implements ApplicationContextAware {
    /**
     * -- GETTER --
     *  获取Spring上下文
     */
    @Getter
    private static ApplicationContext context;

    @Override
    public void setApplicationContext(@Nullable ApplicationContext applicationContext) throws BeansException {
        BeanSupport.context = applicationContext;
    }

    /**
     * 通过类型获取Bean
     */
    public static <T> T getBean(Class<T> clazz) {
        return context.getBean(clazz);
    }

    /**
     * 通过名称获取Bean
     */
    public static Object getBean(String name) {
        return context.getBean(name);
    }

    /**
     * 通过名称和类型获取Bean
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return context.getBean(name, clazz);
    }
}
