package com.cloudmall.framework.global;

import com.cloudmall.framework.utils.Result;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 全局返回值处理器
 *
 * @author Tomatos
 * @date 2025/12/17
 */
@ControllerAdvice
public class GlobalResponseHandler implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return shouldWrapResponse(returnType.getParameterType());
    }

    /**
     * 判断返回值类型是否需要自动包装为 {@link Result}。
     * <p>
     * 如果返回值已经是 Result 或 String 类型，则跳过包装：
     * <ul>
     *   <li>Result 类型避免二次包装（防止双层 Result）</li>
     *   <li>String 类型因 HttpMessageConverter 的特殊处理，若包装为 Result 会抛出异常</li>
     * </ul>
     *
     * @param type 控制器方法的返回值类型
     * @return true 需要包装，false 跳过包装
     */
    private boolean shouldWrapResponse(Class<?> type) {
        return !Result.class.isAssignableFrom(type)
                && !String.class.isAssignableFrom(type);
    }

    @Override
    public Object beforeBodyWrite(@Nullable Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        return Result.success(body);
    }
}