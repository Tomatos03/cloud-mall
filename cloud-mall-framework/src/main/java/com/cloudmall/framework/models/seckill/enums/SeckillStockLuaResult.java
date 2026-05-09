package com.cloudmall.framework.models.seckill.enums;

import java.util.Arrays;

import com.cloudmall.framework.common.enums.BizErrorCode;
import com.cloudmall.framework.exception.BizException;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 秒杀库存 Lua 执行返回码
 */
@Getter
@AllArgsConstructor
public enum SeckillStockLuaResult {
    REPEAT_ORDER(-2, "重复下单"),
    STOCK_NOT_INIT(-3, "库存未预热"),
    STOCK_NOT_ENOUGH(-1, "库存不足");

    private final int code;
    private final String desc;

    public static SeckillStockLuaResult of(int code) {
        return Arrays.stream(values())
                     .filter(item -> item.code == code)
                     .findFirst()
                     .orElseThrow(() -> new BizException(BizErrorCode.SECKILL_FAILED));
    }
}
