package com.cloudmall.framework.models.seckill.enums;

import com.cloudmall.framework.common.enums.BizErrorCode;
import com.cloudmall.framework.exception.BizException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 秒杀活动状态枚举
 * 
 * 定义秒杀活动的三种状态：
 * - REGISTRATION: 报名中（商家可申请）
 * - IN_PROGRESS: 进行中（活动正在进行）
 * - ENDED: 已结束（活动已结束）
 */
@Getter
@AllArgsConstructor
public enum SeckillActivityStatus {
    /**
     * 报名中
     */
    REGISTRATION(0, "报名中"),

    /**
     * 进行中
     */
    IN_PROGRESS(1, "进行中"),

    /**
     * 已结束
     */
    ENDED(2, "已结束");

    private final int code;
    private final String desc;

    public static SeckillActivityStatus of(int code) {
        return Arrays.stream(values())
                     .filter(status -> status.code == code)
                     .findFirst()
                     .orElseThrow(() -> new BizException(BizErrorCode.INVALID_ACTIVITY_STATUS));
    }
}