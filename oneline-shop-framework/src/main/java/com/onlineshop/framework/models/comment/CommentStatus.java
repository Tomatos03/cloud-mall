package com.onlineshop.framework.models.comment;

import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 评论状态枚举
 *
 * @author : Tomatos
 * @date : 2025/12/25
 */
@Getter
@AllArgsConstructor
public enum CommentStatus {
    /**
     * 未评论
     */
    NOT_COMMENTED("NOT_COMMENTED", "未评论", 0),

    /**
     * 已评论
     */
    COMMENTED("COMMENTED", "已评论", 1);

    private final String code;
    private final String desc;
    private final Integer value;

    public static CommentStatus of(String code) {
        return Arrays.stream(values())
                     .filter(commentStatus -> commentStatus.code.equals(code))
                     .findFirst()
                     .orElseThrow(() -> new BusinessException(BizErrorCode.INVALID_PARAM));
    }

    public static CommentStatus ofValue(Integer value) {
        return Arrays.stream(values())
                     .filter(commentStatus -> commentStatus.value.equals(value))
                     .findFirst()
                     .orElseThrow(() -> new BusinessException(BizErrorCode.INVALID_PARAM));
    }
}
