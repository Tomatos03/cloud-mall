package com.onlineshop.framework.exception;

import com.onlineshop.framework.enums.BizErrorCode;
import lombok.Getter;

/**
 *
 *
 * @author : Tomatos
 * @date : 2025/12/18
 */
@Getter
public class BusinessException extends RuntimeException {
    BizErrorCode bizErrorCode;

    public BusinessException(BizErrorCode bizErrorCode) {
        super(bizErrorCode.getErrorMessage());
        this.bizErrorCode = bizErrorCode;
    }
}
