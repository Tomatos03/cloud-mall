package com.onlineshop.framework.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 *
 *
 * @author : Tomatos
 * @date : 2025/12/24
 */
public class OrderNoUtil {
    private static final String PARENT_ORDER_PREFIX = "P";

    /**
     * 生成订单编号
     * 格式：时间戳(14位) + UUID(8位)
     *
     * @return 订单编号
     */
    public static String generateOrderNo() {
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return timestamp + uuid;
    }

    public static String generateParentOrderNo() {
        return PARENT_ORDER_PREFIX + generateOrderNo();
    }
}
