package com.onlineshop.framework.event.cart;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 清空购物车事件
 *
 * @author : Tomatos
 * @date : 2026/1/27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClearCartEvent {
    private Long userId;
    private List<Long> skuIds;
}
