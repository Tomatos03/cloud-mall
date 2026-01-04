package com.onlineshop.framework.models.cart;

import cn.hutool.core.collection.CollectionUtil;
import com.onlineshop.framework.enums.BizErrorCode;
import com.onlineshop.framework.exception.BusinessException;
import com.onlineshop.framework.models.cart.dto.AddCartItemDTO;
import com.onlineshop.framework.models.cart.dto.CartCacheItemDTO;
import com.onlineshop.framework.models.cart.dto.UpdateCartItemDTO;
import com.onlineshop.framework.models.goods.Goods;
import com.onlineshop.framework.models.store.Store;
import com.onlineshop.framework.models.goods.GoodsService;
import com.onlineshop.framework.models.store.StoreService;
import com.onlineshop.framework.utils.context.UserContextHolder;
import com.onlineshop.framework.models.cart.vo.CartStoreItemVO;
import com.onlineshop.framework.models.cart.vo.CartSotreVO;
import com.onlineshop.framework.models.cart.vo.CartVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 购物车服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartService implements ICartService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final GoodsService goodsService;
    private final StoreService storeService;

    private static final String CART_KEY_PREFIX = "cart:";
    private static final long CART_EXPIRE_DAYS = 30;

    /**
     * 添加商品到购物车
     */
    @Override
    public CartStoreItemVO addToCart(AddCartItemDTO addCartItemDTO) {
        Long userId = UserContextHolder.getUserId();
        Long goodsId = addCartItemDTO.getGoodsId();
        Long storeId = addCartItemDTO.getStoreId();

        Goods goods = validateAndGetGoods(goodsId, storeId);
        Store store = validateAndGetStore(storeId);

        String cartKey = getCartKey(userId);
        String fieldKey = getFieldKey(storeId, goodsId);

        Object existingObj = redisTemplate.opsForHash().get(cartKey, fieldKey);
        CartStoreItemVO cartStoreItemVO;
        if (existingObj instanceof CartStoreItemVO tempCartStoreItemVO) {
            cartStoreItemVO = tempCartStoreItemVO;
            long newQuantity = cartStoreItemVO.getQuantity() + addCartItemDTO.getQuantity();
            validateInventory(goods.getInventory(), newQuantity);
            updateCartStoreItemVO(cartStoreItemVO, newQuantity, goods);
        } else {
            cartStoreItemVO = buildCartStoreItemVO(addCartItemDTO, store, goods);
        }

        redisTemplate.opsForHash().put(cartKey, fieldKey, cartStoreItemVO);
        redisTemplate.expire(cartKey, CART_EXPIRE_DAYS, TimeUnit.DAYS);
        return cartStoreItemVO;
    }

    private static void updateCartStoreItemVO(CartStoreItemVO cartStoreItemVO, long newQuantity, Goods goods) {
        cartStoreItemVO.setQuantity(newQuantity);
        cartStoreItemVO.setPrice(goods.getPrice());
        cartStoreItemVO.setInventory(goods.getInventory());
    }

    private static CartStoreItemVO buildCartStoreItemVO(AddCartItemDTO addCartItemDTO, Store store, Goods goods) {
        CartStoreItemVO cartStoreItemVO;
        cartStoreItemVO = CartStoreItemVO.builder()
                                     .storeId(store.getId())
                                     .storeName(store.getName())
                                     .goodsId(goods.getId())
                                     .goodsName(goods.getName())
                                     .price(goods.getPrice())
                                     .selected(false)  // 默认不选中
                                     .inventory(goods.getInventory())
                                     .quantity(addCartItemDTO.getQuantity())
                                     .mainImage(goods.getImg())
                                     .unit(goods.getUnit())
                                     .build();
        return cartStoreItemVO;
    }

    /**
     * 生成购物车 Redis Key
     */
    private String getCartKey(Long userId) {
        return CART_KEY_PREFIX + userId;
    }

    /**
     * 生成购物车项 Field Key
     */
    private String getFieldKey(Long storeId, Long goodsId) {
        return "storeId:" + storeId + ":goodsId:" + goodsId;
    }

    /**
     * 获取购物车
     */
    @Override
    public CartVO getCart() {
        Long userId = UserContextHolder.getUserId();
        String cartKey = getCartKey(userId);
        Map<Object, Object> cartData = redisTemplate.opsForHash().entries(cartKey);

        if (cartData.isEmpty()) {
            return CartVO.builder()
                    .storeList(Collections.emptyList())
                    .build();
        }

        // 按店铺分组
        Map<Long, CartSotreVO> shopMap = new LinkedHashMap<>();
        for (Map.Entry<Object, Object> entry : cartData.entrySet()) {
            try {
                CartStoreItemVO itemVO = (CartStoreItemVO) entry.getValue();
                Long storeId = itemVO.getStoreId();

                if (!shopMap.containsKey(storeId)) {
                    shopMap.put(storeId, CartSotreVO.builder()
                                                    .storeId(storeId)
                                                    .storeName(itemVO.getStoreName())
                                                    .items(new ArrayList<>())
                                                    .build());
                }
                shopMap.get(storeId).getItems().add(itemVO);
            } catch (Exception e) {
                log.error("解析购物车数据失败: {}", entry.getKey(), e);
            }
        }

        return CartVO.builder()
                .storeList(new ArrayList<>(shopMap.values()))
                .build();
    }

    /**
     * 更新购物车项
     */
    @Override
    public CartStoreItemVO updateCartItem(UpdateCartItemDTO request) {
        Long userId = UserContextHolder.getUserId();
        Long storeId = request.getStoreId();
        Long goodsId = request.getGoodsId();

        String cartKey = getCartKey(userId);
        String fieldKey = getFieldKey(storeId, goodsId);

        Object existingObj = redisTemplate.opsForHash().get(cartKey, fieldKey);
        validateCartStoreItem(existingObj);

        CartStoreItemVO cartItem = (CartStoreItemVO) existingObj;

        Goods goods = validateAndGetGoodsForUpdate(goodsId);
        validateInventory(goods.getInventory(), request.getQuantity());
        updateCartStoreItemVO(cartItem, request.getQuantity(), goods);

        redisTemplate.opsForHash().put(cartKey, fieldKey, cartItem);
        redisTemplate.expire(cartKey, CART_EXPIRE_DAYS, TimeUnit.DAYS);
        return cartItem;
    }

    private static void validateCartStoreItem(Object existingObj) {
        if (!(existingObj instanceof CartStoreItemVO)) {
            throw new BusinessException(BizErrorCode.CART_ITEM_NOT_EXIST);
        }
    }

    /**
     * 删除购物车项
     */
    @Override
    public void removeCartItem(Long storeId, Long goodsId) {
        Long userId = UserContextHolder.getUserId();
        String cartKey = getCartKey(userId);
        String fieldKey = getFieldKey(storeId, goodsId);
        redisTemplate.opsForHash().delete(cartKey, fieldKey);
    }

    /**
     * 批量删除购物车项
     */
    @Override
    public void removeCartItems(List<CartCacheItemDTO> itemList) {
        if (CollectionUtil.isEmpty(itemList)) {
            return;
        }

        Long userId = UserContextHolder.getUserId();
        String cartKey = getCartKey(userId);
        Object[] fieldKeys = new Object[itemList.size()];


        int size = itemList.size();
        for (int i = 0; i < size; i++) {
            CartCacheItemDTO item = itemList.get(i);
            fieldKeys[i] = getFieldKey(item.getStoreId(), item.getGoodsId());
        }

        redisTemplate.opsForHash().delete(cartKey, fieldKeys);
    }

    /**
     * 清空购物车
     */
    @Override
    public void clearCart() {
        Long userId = UserContextHolder.getUserId();
        String cartKey = getCartKey(userId);
        redisTemplate.delete(cartKey);
    }

    /**
     * 检查用户购物车中是否存在指定商品
     * 优化版本：不需要遍历所有商品，只需检查任意storeId+goodsId组合是否存在
     */
    @Override
    public boolean existsInCart(Long userId, Long goodsId) {
        String cartKey = getCartKey(userId);
        Map<Object, Object> cartData = redisTemplate.opsForHash().entries(cartKey);
        
        // 遍历所有购物车项，检查是否有匹配的商品ID
        for (Object value : cartData.values()) {
            try {
                CartStoreItemVO item = (CartStoreItemVO) value;
                if (item.getGoodsId().equals(goodsId)) {
                    return true;
                }
            } catch (Exception e) {
                log.error("解析购物车数据失败", e);
            }
        }
        return false;
    }

    /**
     * 验证商品存在性、是否上架以及是否属于指定店铺
     */
    private Goods validateAndGetGoods(Long goodsId, Long storeId) {
        // 验证商品是否存在
        Goods goods = goodsService.getById(goodsId);
        if (goods == null) {
            throw new BusinessException(BizErrorCode.CART_GOODS_NOT_EXIST);
        }

        // 验证店铺是否匹配
        if (!goods.getStoreId().equals(storeId)) {
            throw new BusinessException(BizErrorCode.CART_GOODS_NOT_BELONG_TO_STORE);
        }

        // 验证商品是否上架
        if (Boolean.FALSE.equals(goods.getStatus())) {
            throw new BusinessException(BizErrorCode.CART_GOODS_OFF_SHELF);
        }

        return goods;
    }

    /**
     * 验证商品存在性（用于更新时）
     */
    private Goods validateAndGetGoodsForUpdate(Long goodsId) {
        Goods goods = goodsService.getById(goodsId);
        if (goods == null) {
            throw new BusinessException(BizErrorCode.CART_GOODS_NOT_EXIST);
        }
        return goods;
    }

    /**
     * 验证库存是否充足
     */
    private void validateInventory(Long inventory, Long requiredQuantity) {
        if (inventory == null || inventory < requiredQuantity) {
            throw new BusinessException(BizErrorCode.CART_GOODS_STOCK_INSUFFICIENT);
        }
    }

    /**
     * 验证店铺存在性
     */
    private Store validateAndGetStore(Long storeId) {
        Store store = storeService.getById(storeId);
        if (store == null) {
            throw new BusinessException(BizErrorCode.CART_STORE_NOT_EXIST);
        }
        return store;
    }
}
