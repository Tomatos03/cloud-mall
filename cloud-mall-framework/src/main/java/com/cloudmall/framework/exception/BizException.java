package com.cloudmall.framework.exception;

import com.cloudmall.framework.common.enums.BizErrorCode;
import lombok.Getter;

/**
 *
 *
 * @author : Tomatos
 * @date : 2025/12/18
 */
@Getter
public class BizException extends RuntimeException {
    BizErrorCode bizErrorCode;

    public BizException(BizErrorCode bizErrorCode) {
        super(bizErrorCode.getErrorMessage());
        this.bizErrorCode = bizErrorCode;
    }
}
