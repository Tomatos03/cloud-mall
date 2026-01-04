package com.onlineshop.framework.utils;

import cn.hutool.json.JSONUtil;
import com.onlineshop.framework.enums.BizErrorCode;
import com.onlineshop.framework.exception.BusinessException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

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

    /**
     * 写入业务异常响应
     *
     * @param response              HttpServletResponse
     * @param businessException     业务异常
     * @throws IOException 写入异常
     */
    public static void writeBusinessException(HttpServletResponse response, BusinessException businessException) throws IOException {
        BizErrorCode bizErrorCode = businessException.getBizErrorCode();
        writeErrorResponse(response, bizErrorCode.getErrorMessage(), bizErrorCode.getCode(), 400);
    }

    /**
     * 写入业务异常响应（带自定义消息）
     *
     * @param response              HttpServletResponse
     * @param businessException     业务异常
     * @param customMessage         自定义消息
     * @throws IOException 写入异常
     */
    public static void writeBusinessException(HttpServletResponse response, BusinessException businessException, String customMessage) throws IOException {
        BizErrorCode bizErrorCode = businessException.getBizErrorCode();
        String message = customMessage != null ? customMessage : bizErrorCode.getErrorMessage();
        writeErrorResponse(response, message, bizErrorCode.getCode(), 400);
    }

    /**
     * 写入通用异常响应
     *
     * @param response  HttpServletResponse
     * @param exception 通用异常
     * @throws IOException 写入异常
     */
    public static void writeException(HttpServletResponse response, Exception exception) throws IOException {
        log.error("系统异常", exception);
        writeErrorResponse(response, "服务器内部错误，请联系管理员", Result.INTERNAL_ERROR_CODE, 500);
    }

    /**
     * 写入通用异常响应（带自定义消息）
     *
     * @param response      HttpServletResponse
     * @param exception     通用异常
     * @param customMessage 自定义消息
     * @throws IOException 写入异常
     */
    public static void writeException(HttpServletResponse response, Exception exception, String customMessage) throws IOException {
        log.error("系统异常: {}", customMessage, exception);
        String message = customMessage != null ? customMessage : "服务器内部错误，请联系管理员";
        writeErrorResponse(response, message, Result.INTERNAL_ERROR_CODE, 500);
    }

    /**
     * 写入通用异常响应（带自定义消息和状态码）
     *
     * @param response      HttpServletResponse
     * @param exception     通用异常
     * @param customMessage 自定义消息
     * @param code          自定义状态码
     * @throws IOException 写入异常
     */
    public static void writeException(HttpServletResponse response, Exception exception, String customMessage, int code) throws IOException {
        log.error("系统异常: {}", customMessage, exception);
        String message = customMessage != null ? customMessage : "服务器内部错误，请联系管理员";
        writeErrorResponse(response, message, code, 500);
    }

    /**
     * 写入未授权的错误响应 (401)
     *
     * @param response HttpServletResponse
     * @param message  错误消息
     * @param code     业务错误码
     * @throws IOException 写入异常
     */
    public static void writeUnauthorized(HttpServletResponse response, String message, int code) throws IOException {
        writeErrorResponse(response, message, code, 401);
    }

    /**
     * 写入权限拒绝的错误响应 (403)
     *
     * @param response HttpServletResponse
     * @param message  错误消息
     * @param code     业务错误码
     * @throws IOException 写入异常
     */
    public static void writeForbidden(HttpServletResponse response, String message, int code) throws IOException {
        writeErrorResponse(response, message, code, 403);
    }

    /**
     * 写入资源不存在的错误响应 (404)
     *
     * @param response HttpServletResponse
     * @param message  错误消息
     * @param code     业务错误码
     * @throws IOException 写入异常
     */
    public static void writeNotFound(HttpServletResponse response, String message, int code) throws IOException {
        writeErrorResponse(response, message, code, 404);
    }

    /**
     * 写入错误响应（默认HTTP状态码400）
     *
     * @param response HttpServletResponse
     * @param message  错误消息
     * @param code     业务错误码
     * @throws IOException 写入异常
     */
    public static void writeErrorResponse(HttpServletResponse response, String message, int code) throws IOException {
        writeErrorResponse(response, message, code, 400);
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
    public static void writeErrorResponse(HttpServletResponse response, String message, int code, int httpStatus) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("code", code);
        result.put("message", message);
        result.put("data", null);
        writeResponse(response, result, httpStatus);
    }

    /**
     * 写入成功响应
     *
     * @param response HttpServletResponse
     * @param data     响应数据
     * @throws IOException 写入异常
     */
    public static void writeSuccessResponse(HttpServletResponse response, Object data) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("code", Result.SUCCESS_CODE);
        result.put("message", "操作成功");
        result.put("data", data);
        writeResponse(response, result, 200);
    }

    /**
     * 写入成功响应（带自定义消息）
     *
     * @param response HttpServletResponse
     * @param message  自定义消息
     * @param data     响应数据
     * @throws IOException 写入异常
     */
    public static void writeSuccessResponse(HttpServletResponse response, String message, Object data) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("code", Result.SUCCESS_CODE);
        result.put("message", message);
        result.put("data", data);
        writeResponse(response, result, 200);
    }

    /**
     * 写入自定义响应
     *
     * @param response    HttpServletResponse
     * @param code        业务码
     * @param message     消息
     * @param data        数据
     * @param httpStatus  HTTP状态码
     * @throws IOException 写入异常
     */
    public static void writeCustomResponse(HttpServletResponse response, int code, String message, Object data, int httpStatus) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("code", code);
        result.put("message", message);
        result.put("data", data);
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