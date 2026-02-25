package com.onlineshop.framework.utils;

import cn.hutool.json.JSONUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 响应写入工具类
 * 专门处理在全局异常处理器无法拦截的场景（如拦截器、过滤器等）中
 * 直接向HttpServletResponse写入异常结果
 *
 * @author Tomatos
 * @date 2025/12/18
 */
@Slf4j
public class ResponseWriteUtil {

    private static final String CONTENT_TYPE = "application/json;charset=UTF-8";

    public static void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        writeErrorResponse(response, message, HttpStatus.UNAUTHORIZED.value());
    }

    public static void writeForbidden(HttpServletResponse response, String message) throws IOException {
        writeErrorResponse(response, message, HttpStatus.FORBIDDEN.value());
    }

    /**
     * 写入错误响应（指定HTTP状态码）
     *
     * @param response   HttpServletResponse
     * @param message    错误消息
     * @param code       业务错误码
     * @param httpStatus HTTP状态码
     * @throws IOException 写入异常
     */
    public static void writeErrorResponse(HttpServletResponse response, String message, int httpStatus) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("message", message);
        writeResponse(response, result, httpStatus);
    }

    /**
     * 内部方法：统一处理响应写入
     *
     * @param response    HttpServletResponse
     * @param result      返回结果Map
     * @param httpStatus  HTTP状态码
     * @throws IOException 写入异常
     */
    private static void writeResponse(HttpServletResponse response, Map<String, Object> result, int httpStatus) throws IOException {
        // 设置HTTP状态码
        response.setStatus(httpStatus);
        // 设置响应头
        response.setContentType(CONTENT_TYPE);
        // 设置字符编码
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        // 写入JSON响应
        response.getWriter().write(JSONUtil.toJsonStr(result));
        response.getWriter().flush();
    }
}