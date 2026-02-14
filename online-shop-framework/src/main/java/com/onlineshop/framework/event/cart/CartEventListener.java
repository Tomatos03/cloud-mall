package com.onlineshop.framework.event.cart;

import com.onlineshop.framework.models.cart.ICartService;
import com.onlineshop.framework.utils.AuthUserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 购物车事件监听器 - 处理购物车相关的事务事件
 *
 * @author : Tomatos
 * @date : 2026/1/27
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CartEventListener {
    private final ICartService cartService;

    /**
     * 监听清空购物车事件
     * 在订单创建事务提交之后执行，清理相应SKU的购物车商品
     *
     * @param event 清空购物车事件，包含用户ID和SKU ID列表
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onClearCart(ClearCartEvent event) {
        Long userId = AuthUserUtils.getUserId();
        log.info("开始清理购物车 - userId: {}, skuIds: {}", userId, event.getSkuIds());
        
        try {
            cartService.removeCartItems(event.getSkuIds());
            log.info("购物车清理成功 - userId: {}", userId);
        } catch (Exception e) {
            log.error("购物车清理失败 - userId: {}", userId);
        }
    }
}