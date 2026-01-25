package com.onlineshop.framework.models.order.strategy.impl;

import com.onlineshop.framework.models.cart.CartType;
import com.onlineshop.framework.models.goods.sku.GoodsSku;
import com.onlineshop.framework.models.goods.sku.IGoodsSkuService;
import com.onlineshop.framework.models.order.dto.TradeDTO;
import com.onlineshop.framework.models.order.dto.TradeShopDTO;
import com.onlineshop.framework.models.order.dto.TradeShopItemDTO;
import com.onlineshop.framework.models.goods.spu.Goods;
import com.onlineshop.framework.enums.BizErrorCode;
import com.onlineshop.framework.exception.BusinessException;
import com.onlineshop.framework.models.goods.spu.IGoodsService;
import com.onlineshop.framework.models.order.strategy.OrderValidateStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 抽象订单校验策略基类
 * 职责：定义订单校验的通用逻辑框架
 * 
 * 重要说明（2025-01-02多规格更新）：
 * ============================================================
 * ✅ 校验内容变化：
 *   - 从商品(Goods)级别验证 → SKU级别验证
 *   - 从 TradeShopItemDTO.goodsId → TradeShopItemDTO.skuId
 *   - 库存校验从商品库存 → SKU库存
 * 
 * ✅ 校验流程：
 *   1. SKU存在性校验
 *   2. SKU所属商品的店铺归属验证
 *   3. SKU库存充足性校验
 * ============================================================
 *
 * @author : Tomatos
 * @date : 2025/12/24
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractOrderValidateStrategy implements OrderValidateStrategy {

    protected final IGoodsService goodsService;
    protected final IGoodsSkuService goodsSkuService;

    @Override
    public void validate(TradeDTO tradeDTO) {
        log.info("开始订单校验, cartType: {}", supportCartType());

        // 1. 校验基本数据
        validateBasicData(tradeDTO);

        // 2. 校验每个店铺及其商品
        for (TradeShopDTO shopDTO : tradeDTO.getTradeItems()) {
            validateShop(shopDTO);
        }

        // 3. 子类特定校验(如购物车校验)
        doAdditionalValidate(tradeDTO);

        log.info("订单校验通过, cartType: {}", supportCartType());
    }

    /**
     * 子类特定的额外校验
     *
     * @param tradeDTO 交易数据
     */
    protected void doAdditionalValidate(TradeDTO tradeDTO) {}

    /**
     * 校验基本数据
     */
    protected void validateBasicData(TradeDTO tradeDTO) {
        if (tradeDTO == null) {
            log.error("订单数据为空");
            throw new BusinessException(BizErrorCode.ORDER_DATA_IS_NULL);
        }

        if (tradeDTO.getTradeItems() == null || tradeDTO.getTradeItems().isEmpty()) {
            log.error("订单商品项为空");
            throw new BusinessException(BizErrorCode.ORDER_GOODS_ID_IS_NULL);
        }
    }

    /**
     * 校验店铺及其商品（支持多规格SKU）
     * 1. 批量查询店铺的有效商品(同时校验店铺存在性)
     * 2. 校验SKU归属和库存
     */
    protected void validateShop(TradeShopDTO shopDTO) {
        // 1. 校验店铺数据结构
        validateShopData(shopDTO);

        Long storeId = shopDTO.getStoreId();

        // 2. 直接查询该店铺的所有有效商品(如果店铺不存在,查询结果为空)
        List<Goods> availableGoodsList = goodsService.lambdaQuery()
                .eq(Goods::getStoreId, storeId)
                .eq(Goods::getStatus, true)
                .list();

        if (availableGoodsList == null || availableGoodsList.isEmpty()) {
            log.error("店铺不存在或没有有效商品, storeId: {}", storeId);
            throw new BusinessException(BizErrorCode.GOODS_OR_SHOP_NOT_EXIST);
        }

        // 3. 构建商品ID到商品对象的映射（用于验证SKU所属商品）
        Map<Long, Goods> goodsMap = availableGoodsList.stream()
                .collect(Collectors.toMap(Goods::getId, goods -> goods));

        Set<Long> availableGoodsIds = goodsMap.keySet();

        // 4. 校验每个商品项（基于SKU）
        List<TradeShopItemDTO> itemList = shopDTO.getTradeShopItemList();
        for (TradeShopItemDTO item : itemList) {
            // 校验商品项基本数据
            validateItemData(item);

            Long skuId = item.getSkuId();

            // 获取SKU信息
            GoodsSku sku = goodsSkuService.getById(skuId);
            if (sku == null) {
                log.error("SKU不存在或已下架, skuId: {}, storeId: {}", skuId, storeId);
                throw new BusinessException(BizErrorCode.GOODS_NOT_EXIST);
            }

            // 校验SKU所属商品是否属于该店铺且已上架
            Long goodsId = sku.getGoodsId();
            if (!availableGoodsIds.contains(goodsId)) {
                log.error("商品不存在、未上架或不属于该店铺, goodsId: {}, skuId: {}, storeId: {}", goodsId, skuId, storeId);
                throw new BusinessException(BizErrorCode.GOODS_NOT_EXIST);
            }

            // 校验SKU库存
            validateSkuInventory(sku, item.getQuantity());
        }

        log.info("店铺及商品校验通过, storeId: {}, 商品数: {}", storeId, itemList.size());
    }

    /**
     * 校验店铺数据结构
     */
    protected void validateShopData(TradeShopDTO shopDTO) {
        if (shopDTO == null || shopDTO.getStoreId() == null) {
            log.error("店铺信息为空");
            throw new BusinessException(BizErrorCode.GOODS_OR_SHOP_NOT_EXIST);
        }
        if (shopDTO.getTradeShopItemList() == null || shopDTO.getTradeShopItemList().isEmpty()) {
            log.error("店铺下商品项为空, storeId: {}", shopDTO.getStoreId());
            throw new BusinessException(BizErrorCode.ORDER_GOODS_ID_IS_NULL);
        }
    }

    /**
     * 校验商品项基本数据（支持多规格SKU）
     */
    protected void validateItemData(TradeShopItemDTO item) {
        if (item == null) {
            log.error("商品项数据为空");
            throw new BusinessException(BizErrorCode.ORDER_DATA_IS_NULL);
        }
        if (item.getSkuId() == null) {
            log.error("SKU ID为空");
            throw new BusinessException(BizErrorCode.ORDER_GOODS_ID_IS_NULL);
        }
        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            log.error("商品数量无效, quantity: {}", item.getQuantity());
            throw new BusinessException(BizErrorCode.ORDER_QUANTITY_INVALID);
        }
    }

    /**
     * 校验SKU库存（替代原有的商品库存校验）
     *
     * @param sku      SKU信息
     * @param quantity 购买数量
     */
    protected void validateSkuInventory(GoodsSku sku, Integer quantity) {
        if (sku.getInventory() == null || sku.getInventory() < quantity) {
            log.error("SKU库存不足, skuId: {}, 需要数量: {}, 当前库存: {}",
                    sku.getId(), quantity, sku.getInventory());
            throw new BusinessException(BizErrorCode.GOODS_INVENTORY_NOT_ENOUGH);
        }
    }

    /**
     * 获取支持的购物车类型（子类实现）
     *
     * @return 购物车类型
     */
    @Override
    public abstract CartType supportCartType();
}