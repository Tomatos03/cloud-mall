package com.onlineshop.framework.event.cart;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 清空购物车事件
 *
 * @author : Tomatos
 * @date : 2026/1/27
 */
@Data
@Builder
public class ClearCartEvent {
    private List<Long> skuIds;
}
