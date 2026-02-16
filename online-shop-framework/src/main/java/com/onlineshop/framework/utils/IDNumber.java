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
public class IDNumber {
    private static final String PARENT_ORDER_ID_PREFIX = "P";
    private static final String STORE_ID_PREFIX = "S";

    public static String generateStoreNo() {
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return STORE_ID_PREFIX + timestamp + uuid;
    }

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
        return PARENT_ORDER_ID_PREFIX + generateOrderNo();
    }
}