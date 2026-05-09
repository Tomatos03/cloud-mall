package com.cloudmall.framework.models.cart;

import cn.hutool.core.collection.CollUtil;
import com.cloudmall.framework.common.enums.BizErrorCode;
import com.cloudmall.framework.exception.BizException;
import com.cloudmall.framework.models.cart.dto.AddCartItemDTO;
import com.cloudmall.framework.models.cart.dto.UpdateCartItemDTO;
import com.cloudmall.framework.models.cart.vo.CartStoreItemVO;
import com.cloudmall.framework.models.cart.vo.CartStoreVO;
import com.cloudmall.framework.models.cart.vo.CartVO;
import com.cloudmall.framework.models.goods.sku.GoodsSku;
import com.cloudmall.framework.models.goods.sku.IGoodsSkuService;
import com.cloudmall.framework.models.goods.spec.entity.GoodsSkuSpec;
import com.cloudmall.framework.models.goods.spec.entity.Spec;
import com.cloudmall.framework.models.goods.spec.entity.SpecValue;
import com.cloudmall.framework.models.goods.spec.service.IGoodsSkuSpecService;
import com.cloudmall.framework.models.goods.spec.service.ISpecService;
import com.cloudmall.framework.models.goods.spec.service.ISpecValueService;
import com.cloudmall.framework.models.goods.spu.Goods;
import com.cloudmall.framework.models.goods.spu.GoodsService;
import com.cloudmall.framework.models.store.Store;
import com.cloudmall.framework.models.store.StoreService;
import com.cloudmall.framework.utils.AuthUserUtils;
import com.cloudmall.framework.utils.image.ImageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 购物车服务 - 支持多规格商品
 * 使用 Spring Cache 注解和 RedisTemplate 完成缓存和数据存储
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartService implements ICartService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final GoodsService goodsService;
    private final StoreService storeService;
    private final IGoodsSkuService goodsSkuService;
    private final IGoodsSkuSpecService goodsSkuSpecService;
    private final ISpecService specService;
    private final ISpecValueService specValueService;

    private static final String CART_KEY_PREFIX = "cart:";
    private static final long CART_EXPIRE_DAYS = 30;

    /**
     * 获取缓存键中使用的用户ID
     * 用于@CacheEvict注解的SpEL表达式
     */
    public Long getUserIdForCache() {
        return AuthUserUtils.getUserId();
    }

    /**
     * 添加商品到购物车
     * 前端仅传入SKU ID和购买数量，后端从SKU构建完整的商品和店铺信息
     */
    @Override
    @CacheEvict(value = "cart", key = "#root.target.getUserIdForCache()")
    public CartStoreItemVO addToCart(AddCartItemDTO addCartItemDTO) {
        Long userId = AuthUserUtils.getUserId();
        Long skuId = addCartItemDTO.getSkuId();

        // 从SKU获取完整的商品和店铺信息
        GoodsSku sku = validateSku(skuId);

        Long goodsId = sku.getGoodsId();
        Goods goods = validateGoods(goodsId);

        Long storeId = goods.getStoreId();
        Store store = validateStore(storeId);

        String cartKey = getCartKey(userId);
        String fieldKey = getFieldKey(storeId, goodsId, skuId);

        Object existingObj = redisTemplate.opsForHash()
                                          .get(cartKey, fieldKey);
        CartStoreItemVO cartStoreItemVO;
        if (existingObj instanceof CartStoreItemVO tempCartStoreItemVO) {
            // 已存在，更新数量
            cartStoreItemVO = tempCartStoreItemVO;
            long newQuantity = cartStoreItemVO.getQuantity() + addCartItemDTO.getQuantity();
            cartStoreItemVO.setQuantity(newQuantity);
        } else {
            // 新增，构建购物车项
            cartStoreItemVO = buildCartStoreItemVO(addCartItemDTO, store, goods, sku);
        }

        redisTemplate.opsForHash()
                     .put(cartKey, fieldKey, cartStoreItemVO);
        redisTemplate.expire(cartKey, CART_EXPIRE_DAYS, TimeUnit.DAYS);
        return cartStoreItemVO;
    }

    /**
     * 获取购物车
     */
    @Override
    public CartVO getCart() {
        Long userId = AuthUserUtils.getUserId();
        String cartKey = getCartKey(userId);
        Map<Object, Object> cartData = redisTemplate.opsForHash()
                                                    .entries(cartKey);

        if (cartData.isEmpty()) {
            return CartVO.builder()
                         .storeList(Collections.emptyList())
                         .build();
        }

        // 按店铺分组
        Map<Long, CartStoreVO> shopMap = new LinkedHashMap<>();
        for (Map.Entry<Object, Object> entry : cartData.entrySet()) {
            try {
                CartStoreItemVO itemVO = (CartStoreItemVO) entry.getValue();
                Long storeId = itemVO.getStoreId();

                if (!shopMap.containsKey(storeId)) {
                    shopMap.put(storeId, CartStoreVO.builder()
                                                    .storeId(storeId)
                                                    .storeName(itemVO.getStoreName())
                                                    .items(new ArrayList<>())
                                                    .build());
                }
                shopMap.get(storeId)
                       .getItems()
                       .add(itemVO);
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
     * 前端仅需传入SKU ID和新的购买数量
     */
    @Override
    @CacheEvict(value = "cart", key = "#root.target.getUserIdForCache()")
    public CartStoreItemVO updateCartItem(UpdateCartItemDTO request) {
        Long userId = AuthUserUtils.getUserId();
        Long skuId = request.getSkuId();
        String cartKey = getCartKey(userId);

        // 从购物车中查找对应的项
        Map<Object, Object> cartData = redisTemplate.opsForHash()
                                                    .entries(cartKey);
        CartStoreItemVO cartItem = null;
        Object fieldKey = null;

        for (Map.Entry<Object, Object> entry : cartData.entrySet()) {
            try {
                CartStoreItemVO itemVO = (CartStoreItemVO) entry.getValue();
                if (itemVO.getSkuId()
                          .equals(skuId)) {
                    cartItem = itemVO;
                    fieldKey = entry.getKey();
                    break;
                }
            } catch (Exception e) {
                log.error("解析购物车数据失败: {}", entry.getKey(), e);
            }
        }

        if (cartItem == null) {
            throw new BizException(BizErrorCode.CART_ITEM_NOT_EXIST);
        }

        // 更新数量
        cartItem.setQuantity(request.getQuantity());

        redisTemplate.opsForHash()
                     .put(cartKey, fieldKey, cartItem);
        redisTemplate.expire(cartKey, CART_EXPIRE_DAYS, TimeUnit.DAYS);
        return cartItem;
    }

    /**
     * 删除购物车项 - 根据SKU ID删除
     * SKU ID能唯一确定购物车中的一个商品项，无需店铺ID
     */
    @Override
    @CacheEvict(value = "cart", key = "#root.target.getUserIdForCache()")
    public void removeCartItem(Long skuId) {
        Long userId = AuthUserUtils.getUserId();
        String cartKey = getCartKey(userId);
        Map<Object, Object> cartData = redisTemplate.opsForHash()
                                                    .entries(cartKey);

        // 遍历查找匹配skuId的项
        List<Object> fieldsToDelete = new ArrayList<>();
        for (Map.Entry<Object, Object> entry : cartData.entrySet()) {
            try {
                CartStoreItemVO itemVO = (CartStoreItemVO) entry.getValue();
                if (itemVO.getSkuId()
                          .equals(skuId)) {
                    fieldsToDelete.add(entry.getKey());
                }
            } catch (Exception e) {
                log.error("解析购物车数据失败: {}", entry.getKey(), e);
            }
        }

        // 删除找到的项
        if (!fieldsToDelete.isEmpty()) {
            redisTemplate.opsForHash()
                         .delete(cartKey, fieldsToDelete.toArray());
        }
    }

    /**
     * 批量删除购物车项 - 根据SKU ID列表删除
     * 遍历购物车数据，删除与SKU ID匹配的所有项
     */
    @Override
    @CacheEvict(value = "cart", key = "#root.target.getUserIdForCache()")
    public void removeCartItems(Collection<Long> ids) {
        removeCartItemsByUserId(AuthUserUtils.getUserId(), ids);
    }

    @Override
    @CacheEvict(value = "cart", key = "#p0")
    public void removeCartItems(Long userId, Collection<Long> ids) {
        removeCartItemsByUserId(userId, ids);
    }

    /**
     * 清空购物车
     */
    @Override
    @CacheEvict(value = "cart", key = "#root.target.getUserIdForCache()", allEntries = true)
    public void clearCart() {
        Long userId = AuthUserUtils.getUserId();
        String cartKey = getCartKey(userId);
        redisTemplate.delete(cartKey);
    }

    /**
     * 检查用户购物车中是否存在指定商品
     */
    @Override
    public boolean existsInCart(Long userId, Long skuId) {
        String cartKey = getCartKey(userId);
        Map<Object, Object> cartData = redisTemplate.opsForHash()
                                                    .entries(cartKey);

        for (Object value : cartData.values()) {
            try {
                CartStoreItemVO item = (CartStoreItemVO) value;
                if (item.getSkuId()
                        .equals(skuId)) {
                    return true;
                }
            } catch (Exception e) {
                log.error("解析购物车数据失败", e);
            }
        }
        return false;
    }

    /**
     * 验证SKU存在性和上架状态
     */
    private GoodsSku validateSku(Long skuId) {
        GoodsSku sku = goodsSkuService.getById(skuId);
        if (sku == null) {
            throw new BizException(BizErrorCode.CART_GOODS_NOT_EXIST);
        }

        if (Boolean.FALSE.equals(sku.getStatus())) {
            throw new BizException(BizErrorCode.CART_GOODS_OFF_SHELF);
        }
        return sku;
    }

    /**
     * 验证商品存在性和上架状态
     */
    private Goods validateGoods(Long goodsId) {
        Goods goods = goodsService.getById(goodsId);
        if (goods == null) {
            throw new BizException(BizErrorCode.CART_GOODS_NOT_EXIST);
        }

        if (Boolean.FALSE.equals(goods.getStatus())) {
            throw new BizException(BizErrorCode.CART_GOODS_OFF_SHELF);
        }

        return goods;
    }

    /**
     * 验证店铺存在性
     */
    private Store validateStore(Long storeId) {
        Store store = storeService.getById(storeId);
        if (store == null) {
            throw new BizException(BizErrorCode.CART_STORE_NOT_EXIST);
        }
        return store;
    }

    /**
     * 生成购物车 Redis Key
     */
    private String getCartKey(Long userId) {
        return CART_KEY_PREFIX + userId;
    }

    /**
     * 生成购物车项 Field Key
     * 由storeId + goodsId + skuId组合而成，确保多规格商品可以分开存储
     */
    private String getFieldKey(Long storeId, Long goodsId, Long skuId) {
        return "storeId:" + storeId + ":goodsId:" + goodsId + ":skuId:" + skuId;
    }

    private void removeCartItemsByUserId(Long userId, Collection<Long> ids) {
        if (userId == null || CollUtil.isEmpty(ids)) {
            return;
        }

        String cartKey = getCartKey(userId);
        Map<Object, Object> cartData = redisTemplate.opsForHash()
                                                    .entries(cartKey);
        HashSet<Long> deleteIdsSet = new HashSet<>(ids);
        List<Object> fieldsToDelete = new ArrayList<>();
        for (Map.Entry<Object, Object> entry : cartData.entrySet()) {
            try {
                CartStoreItemVO itemVO = (CartStoreItemVO) entry.getValue();
                if (deleteIdsSet.contains(itemVO.getSkuId())) {
                    fieldsToDelete.add(entry.getKey());
                }
            } catch (Exception e) {
                log.error("解析购物车数据失败: {}", entry.getKey(), e);
            }
        }

        if (CollUtil.isNotEmpty(fieldsToDelete)) {
            redisTemplate.opsForHash()
                         .delete(cartKey, fieldsToDelete.toArray());
        }
    }

    /**
     * 构建购物车项VO
     */
    private CartStoreItemVO buildCartStoreItemVO(AddCartItemDTO addCartItemDTO, Store store,
                                                 Goods goods, GoodsSku sku) {
        // 从数据库查询SKU的规格信息
        Map<String, String> skuSpecs = buildSkuSpecsMap(sku.getId());

        return buildCartStoreItemVO(addCartItemDTO, store, goods, sku, skuSpecs);
    }

    /**
     * 根据SKU ID构建规格信息Map
     * 格式: {"颜色": "黑色", "容量": "128G"}
     */
    private Map<String, String> buildSkuSpecsMap(Long skuId) {
        Map<String, String> specsMap = new LinkedHashMap<>();

        try {
            // 查询SKU关联的所有规格
            List<GoodsSkuSpec> skuSpecs = goodsSkuSpecService.listBySkuId(skuId);

            for (GoodsSkuSpec skuSpec : skuSpecs) {
                Spec spec = specService.getById(skuSpec.getSpecId());
                SpecValue specValue = specValueService.getById(skuSpec.getSpecValueId());

                if (spec != null && specValue != null) {
                    specsMap.put(spec.getName(), specValue.getValue());
                }
            }
        } catch (Exception e) {
            log.error("构建SKU规格信息失败, skuId: {}", skuId, e);
        }

        return specsMap;
    }

    private CartStoreItemVO buildCartStoreItemVO(
            AddCartItemDTO addCartItemDTO,
            Store store,
            Goods goods,
            GoodsSku sku,
            Map<String, String> skuSpecs
    ) {
        return CartStoreItemVO.builder()
                              .storeId(store.getId())
                              .storeName(store.getName())
                              .goodsId(goods.getId())
                              .goodsName(goods.getName())
                              .skuId(sku.getId())
                              .skuSpecs(skuSpecs)
                              .price(sku.getPrice())
                              .selected(false)
                              .quantity(addCartItemDTO.getQuantity())
                              .mainImage(ImageUtil.getMainImageUrl(goods.getDisplayImages()))
                              .unit(goods.getUnitName())
                              .build();
    }
}
