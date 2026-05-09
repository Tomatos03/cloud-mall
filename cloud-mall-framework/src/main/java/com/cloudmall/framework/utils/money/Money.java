package com.cloudmall.framework.utils.money;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/1/24
 */
@Data
public final class Money implements Serializable {
    /** 金额，单位：分 */
    private final long cents;

    private Money(long cents) {
        if (cents < 0) {
            throw new IllegalArgumentException("金额不能为负数");
        }
        this.cents = cents;
    }

    public static Money ofCents(long cents) {
        return new Money(cents);
    }

    public static Money ofYuan(String yuan) {
        BigDecimal bd = new BigDecimal(yuan);
        return ofYuan(bd);
    }

    public static Money ofYuan(BigDecimal yuan) {
        long cents = yuan.multiply(BigDecimal.valueOf(100))
                         .setScale(0, RoundingMode.HALF_UP)
                         .longValueExact();
        return new Money(cents);
    }

    public long getCents() {
        return cents;
    }

    public String toYuanString() {
        return toYuan().toPlainString();
    }

    public BigDecimal toYuan() {
        return BigDecimal.valueOf(cents)
                         .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal toYuan(long cent) {
        return BigDecimal.valueOf(cent)
                         .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public Money add(Money other) {
        return new Money(Math.addExact(this.cents, other.cents));
    }

    public Money sub(Money other) {
        long result = Math.subtractExact(this.cents, other.cents);
        if (result < 0) {
            throw new IllegalArgumentException("金额不足");
        }
        return new Money(result);
    }

    /** 乘法（如：单价 × 数量） */
    public Money mul(long multiplier) {
        if (multiplier < 0) {
            throw new IllegalArgumentException("乘数不能为负");
        }
        return new Money(Math.multiplyExact(this.cents, multiplier));
    }

    /** 乘比例（如：折扣、税率） */
    public Money mul(BigDecimal rate) {
        long result = BigDecimal
                .valueOf(this.cents)
                .multiply(rate)
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
        return new Money(result);
    }

    public boolean greater(Money other) {
        return this.cents > other.cents;
    }

    public boolean less(Money other) {
        return this.cents < other.cents;
    }
}
