package com.onlineshop.framework.models.order.utils;

import cn.hutool.core.util.StrUtil;
import com.onlineshop.framework.models.order.entity.Order;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单相关工具方法
 */
public final class OrderUtil {

    private OrderUtil() {
    }

    /**
     * 提取订单ID列表
     */
    public static List<Long> toOrderIds(List<Order> orders) {
        return orders.stream()
                     .map(Order::getId)
                     .toList();
    }

    public static List<Long> toStoreIds(List<Order> orders) {
        return orders.stream()
                     .map(Order::getStoreId)
                     .toList();
    }

    /**
     * 解析SKU规格字符串 "颜色=红;尺寸=L" -> Map
     */
    public static Map<String, String> parseSkuSpecs(String skuSpecs) {
        if (StrUtil.isBlank(skuSpecs)) {
            return Collections.emptyMap();
        }
        Map<String, String> specMap = new LinkedHashMap<>();
        for (String pair : skuSpecs.split(";")) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                specMap.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }
        return specMap;
    }

    public static boolean isAllCanceled(List<Order> orders) {
        return orders.stream()
                     .allMatch(Order::isCanceled);
    }
}
