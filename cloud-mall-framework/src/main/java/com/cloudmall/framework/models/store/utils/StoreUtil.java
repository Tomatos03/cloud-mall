package com.cloudmall.framework.models.store.utils;

import com.cloudmall.framework.models.store.Store;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 店铺工具类
 */
public final class StoreUtil {

    private StoreUtil() {
    }

    /**
     * 店铺列表 -> storeId 映射 storeName
     */
    public static Map<Long, String> toIdToNameMap(List<Store> stores) {
        return stores.stream()
                     .collect(Collectors.toMap(Store::getId, Store::getName));
    }
}
