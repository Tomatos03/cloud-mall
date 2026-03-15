package com.onlineshop.framework.global;

import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.exception.BizException;
import com.onlineshop.framework.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * @author : Tomatos
 * @date : 2025/12/18
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    private final static String INTERNAL_ERROR = "服务器内部错误, 请联系管理员";

    @ExceptionHandler(BizException.class)
    public Result<Void> handleBusinessException(BizException e) {
        BizErrorCode bizError = e.getBizErrorCode();
        return Result.error(bizError.getErrorMessage(), bizError.getCode());
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("服务器内部异常", e);
        return Result.error(INTERNAL_ERROR);
    }
}
