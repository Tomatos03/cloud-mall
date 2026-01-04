package com.onlineshop.framework.utils;

import java.util.Objects;

/**
 *
 * @author Tomatos
 * @date 2025/12/24
 */
public class MoneyUtil {

    /**
     * 分转元（返回字符串，用于前端显示）
     *
     * @param fen 金额（单位：分）
     * @return 金额字符串（单位：元，两位小数）
     */
    public static String fenToYuan(Long fen) {
        Objects.requireNonNull(fen);
        String moneyStr = fen.toString();
        return formatYuanString(moneyStr);
    }

    private static String formatYuanString(String moneyStr) {
        int length = moneyStr.length();
        if (length <= 2) {
            return "0." + String.format("%02d", Integer.parseInt(moneyStr));
        } else {
            String integerPart = moneyStr.substring(0, length - 2);
            String decimalPart = moneyStr.substring(length - 2);
            return integerPart + "." + decimalPart;
        }
    }
}