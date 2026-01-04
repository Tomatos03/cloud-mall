package com.onlineshop.framework.models.cart;

import com.onlineshop.framework.models.cart.dto.AddCartItemDTO;
import com.onlineshop.framework.models.cart.dto.CartCacheItemDTO;
import com.onlineshop.framework.models.cart.dto.UpdateCartItemDTO;
import com.onlineshop.framework.models.cart.vo.CartStoreItemVO;
import com.onlineshop.framework.models.cart.vo.CartVO;

import java.util.List;

/**
 * 购物车服务接口
 */
public interface ICartService {
    
    /**
     * 添加商品到购物车
     * @param addCartItemDTO 添加购物车项DTO
     * @return 购物车项VO
     */
    CartStoreItemVO addToCart(AddCartItemDTO addCartItemDTO);

    /**
     * 获取购物车
     * @return 购物车VO（按店铺分组）
     */
    CartVO getCart();

    /**
     * 更新购物车项
     * @param request 更新购物车项DTO
     * @return 更新后的购物车项VO
     */
    CartStoreItemVO updateCartItem(UpdateCartItemDTO request);

    /**
     * 删除购物车项
     * @param storeId 店铺ID
     * @param goodsId 商品ID
     */
    void removeCartItem(Long storeId, Long goodsId);

    /**
     * 批量删除购物车项
     * @param itemList 购物车项列表
     */
    void removeCartItems(List<CartCacheItemDTO> itemList);

    /**
     * 清空购物车
     */
    void clearCart();

    /**
     * 检查用户购物车中是否存在指定商品
     * @param userId 用户ID
     * @param goodsId 商品ID
     * @return 是否存在
     */
    boolean existsInCart(Long userId, Long goodsId);
}