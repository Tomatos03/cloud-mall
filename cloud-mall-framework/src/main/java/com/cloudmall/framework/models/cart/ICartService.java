package com.cloudmall.framework.models.cart;

import com.cloudmall.framework.models.cart.dto.AddCartItemDTO;
import com.cloudmall.framework.models.cart.dto.UpdateCartItemDTO;
import com.cloudmall.framework.models.cart.vo.CartStoreItemVO;
import com.cloudmall.framework.models.cart.vo.CartVO;

import java.util.Collection;

/**
 * 购物车服务接口 - 支持多规格商品
 */
public interface ICartService {
    
    /**
     * 添加商品到购物车
     * 前端仅需传入SKU ID和购买数量，后端会从SKU自动查询关联的商品和店铺信息
     *
     * @param addCartItemDTO 添加购物车项DTO（仅需包含SKU ID和购买数量）
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
     * @param request 更新购物车项DTO（包含SKU ID）
     * @return 更新后的购物车项VO
     */
    CartStoreItemVO updateCartItem(UpdateCartItemDTO request);

    /**
     * 删除购物车项
     * 根据SKU ID删除对应的购物车项，SKU ID能唯一确定购物车中的一个商品
     *
     * @param skuId SKU ID
     */
    void removeCartItem(Long skuId);

    /**
     * 批量删除购物车项
     * @param itemList 购物车项列表（包含SKU ID）
     */
    void removeCartItems(Collection<Long> ids);

    /**
     * 指定用户批量删除购物车项
     * @param userId 用户ID
     * @param ids SKU ID 列表
     */
    void removeCartItems(Long userId, Collection<Long> ids);

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
