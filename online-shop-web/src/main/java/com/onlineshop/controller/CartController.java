package com.onlineshop.controller;

import com.onlineshop.framework.models.cart.dto.AddCartItemDTO;
import com.onlineshop.framework.models.cart.dto.CartCacheItemDTO;
import com.onlineshop.framework.models.cart.dto.UpdateCartItemDTO;
import com.onlineshop.framework.models.cart.ICartService;
import com.onlineshop.framework.models.cart.vo.CartStoreItemVO;
import com.onlineshop.framework.models.cart.vo.CartVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     * 删除购物车项
     */
    @DeleteMapping("{storeId}/items/{goodsId}")
    public void removeCartItem(@PathVariable Long storeId, @PathVariable Long goodsId) {
        cartService.removeCartItem(storeId, goodsId);
    }

    @PostMapping("/items/batch")
    public void batchRemove(@RequestBody List<CartCacheItemDTO> items) {
        cartService.removeCartItems(items);
    }

    /**
     * 清空购物车
     */
    @DeleteMapping("/clear")
    public void clearCart() {
        cartService.clearCart();
    }
}