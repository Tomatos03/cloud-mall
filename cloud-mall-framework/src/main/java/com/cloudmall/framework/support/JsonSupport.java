package com.cloudmall.framework.support;

import com.cloudmall.framework.context.BeanContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Json工具类，支持对象与Json字符串互转，懒加载ObjectMapper。
 *
 * @author : Tomatos
 * @date : 2026/1/10
 */
public class JsonSupport {
    private static volatile ObjectMapper objectMapper;

    private static ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            synchronized (JsonSupport.class) {
                if (objectMapper == null) {
                    objectMapper = BeanContext.getBean(ObjectMapper.class);
                }
            }
        }
        return objectMapper;
    }

    /**
     * 对象转Json字符串
     */
    public static String toJson(Object obj) {
        try {
            return getObjectMapper().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Json序列化失败", e);
        }
    }

    /**
     * Json字符串转对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return getObjectMapper().readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Json反序列化失败", e);
        }
    }
}
