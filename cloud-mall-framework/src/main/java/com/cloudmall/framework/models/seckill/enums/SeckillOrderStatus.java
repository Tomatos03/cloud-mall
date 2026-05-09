package com.cloudmall.framework.models.seckill.enums;

import java.util.Arrays;

import com.cloudmall.framework.common.enums.BizErrorCode;
import com.cloudmall.framework.exception.BizException;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 秒杀订单状态
 */
@Getter
@AllArgsConstructor
public enum SeckillOrderStatus {
    UNPAID(0, "未支付"),
    PAID(1, "已支付"),
    CANCELED(2, "已取消");

    private final Integer code;
    private final String desc;

    public static SeckillOrderStatus of(int code) {
        return Arrays.stream(values())
                     .filter(item -> item.code == code)
                     .findFirst()
                     .orElseThrow(() -> new BizException(BizErrorCode.SECKILL_ORDER_INVALID_STATUS));
    }
}
