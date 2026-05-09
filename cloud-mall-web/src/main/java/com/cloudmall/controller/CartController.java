package com.cloudmall.controller;

import com.cloudmall.framework.models.cart.ICartService;
import com.cloudmall.framework.models.cart.dto.AddCartItemDTO;
import com.cloudmall.framework.models.cart.dto.RemoveCartItemDTO;
import com.cloudmall.framework.models.cart.dto.UpdateCartItemDTO;
import com.cloudmall.framework.models.cart.vo.CartStoreItemVO;
import com.cloudmall.framework.models.cart.vo.CartVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 购物车控制器
 *
 * @author : Tomatos
 * @date : 2025/12/23
 */
@RestController
@RequestMapping("/web/cart")
public class CartController {

    @Autowired
    private ICartService cartService;

    /**
     * 添加商品到购物车
     */
    @PostMapping
    public CartStoreItemVO addToCart(@Valid @RequestBody AddCartItemDTO addCartItemDTO) {
        return cartService.addToCart(addCartItemDTO);
    }

    /**
     * 获取购物车
     */
    @GetMapping
    public CartVO getCart() {
        return cartService.getCart();
    }

    /**
     * 更新购物车项
     */
    @PutMapping
    public CartStoreItemVO updateCartItem(@Valid @RequestBody UpdateCartItemDTO updateCartItemDTO) {
        return cartService.updateCartItem(updateCartItemDTO);
    }

    /**
     * 删除购物车项 - 根据SKU ID删除
     */
    @DeleteMapping("/{skuId}")
    public void removeCartItem(@PathVariable Long skuId) {
        cartService.removeCartItem(skuId);
    }

    /**
     * 批量删除购物车项 - 根据SKU ID列表批量删除
     */
    @DeleteMapping("/batch")
    public void batchRemove(@RequestBody RemoveCartItemDTO items) {
        cartService.removeCartItems(items.getSkuIds());
    }

    /**
     * 清空购物车
     */
    @DeleteMapping("/clear")
    public void clearCart() {
        cartService.clearCart();
    }
}