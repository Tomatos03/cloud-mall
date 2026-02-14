package com.onlineshop.framework.utils.money;

import java.util.Collection;
import java.util.Objects;

/**
 * 金额工具类
 *
 * @author Tomatos
 * @date 2025/12/24
 */
public final class MoneyUtil {
    private MoneyUtil() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * 对多个Money对象求和
     *
     * @param monies 金额对象数组
     * @return 求和结果，如果数组为空则返回Money.ofCents(0)
     */
    public static Money sum(Money... monies) {
        if (monies == null || monies.length == 0) {
            return Money.ofCents(0);
        }

        long totalCents = 0;
        for (Money money : monies) {
            if (money != null) {
                totalCents = Math.addExact(totalCents, money.getCents());
            }
        }
        return Money.ofCents(totalCents);
    }

    /**
     * 对Money集合求和
     *
     * @param monies 金额对象集合
     * @return 求和结果，如果集合为空则返回Money.ofCents(0)
     */
    public static Money sum(Collection<Money> monies) {
        Objects.requireNonNull(monies, "money collection cannot be null");

        long totalCents = 0;
        for (Money money : monies) {
            if (money != null) {
                totalCents = Math.addExact(totalCents, money.getCents());
            }
        }
        return Money.ofCents(totalCents);
    }
}

