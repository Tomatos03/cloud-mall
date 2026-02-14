package com.onlineshop.framework.models.message;

import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.exception.BizException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 消息状态枚举
 *
 * @author : Tomatos
 * @date : 2026/1/26
 */
@Getter
@AllArgsConstructor
public enum MessageStatus {
    /**
     * 待处理
     */
    PENDING("0", "待处理"),

    /**
     * 成功
     */
    SUCCESS("1", "成功"),

    /**
     * 失败
     */
    FAILED("2", "失败"),

    /**
     * 已结束
     */
    FINISHED("3", "已结束");

    private final String code;
    private final String desc;

    public static MessageStatus of(String code) {
        return Arrays.stream(values())
                     .filter(messageStatus -> messageStatus.code.equals(code))
                     .findFirst()
                     .orElseThrow(() -> new BizException(BizErrorCode.INVALID_MESSAGE_STATUS));
    }
}