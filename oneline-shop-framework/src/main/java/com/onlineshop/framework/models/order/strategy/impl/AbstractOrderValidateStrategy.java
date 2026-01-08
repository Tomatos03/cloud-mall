package com.onlineshop.framework.models.order.strategy.impl;

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
 * 提供公共的校验逻辑
 *
 * @author : Tomatos
 * @date : 2025/12/24
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractOrderValidateStrategy implements OrderValidateStrategy {

    protected final IGoodsService goodsService;

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
    protected void doAdditionalValidate(TradeDTO tradeDTO) {
        // 默认无额外校验，子类按需覆盖
    }

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
     * 校验店铺及其商品
     * 1. 批量查询店铺的有效商品(同时校验店铺存在性)
     * 2. 校验商品归属和库存
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

        // 3. 构建商品ID到商品对象的映射
        Map<Long, Goods> goodsMap = availableGoodsList.stream()
                .collect(Collectors.toMap(Goods::getId, goods -> goods));

        Set<Long> availableGoodsIds = goodsMap.keySet();

        // 4. 校验每个商品项
        List<TradeShopItemDTO> itemList = shopDTO.getTradeShopItemList();
        for (TradeShopItemDTO item : itemList) {
            // 校验商品项基本数据
            validateItemData(item);

            Long goodsId = item.getGoodsId();

            // 校验商品是否属于该店铺且已上架
            if (!availableGoodsIds.contains(goodsId)) {
                log.error("商品不存在、未上架或不属于该店铺, goodsId: {}, storeId: {}", goodsId, storeId);
                throw new BusinessException(BizErrorCode.GOODS_NOT_EXIST);
            }

            // 校验库存
            Goods goods = goodsMap.get(goodsId);
            validateInventory(goods, item.getQuantity());
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
     * 校验商品项基本数据
     */
    protected void validateItemData(TradeShopItemDTO item) {
        if (item == null) {
            log.error("商品项数据为空");
            throw new BusinessException(BizErrorCode.ORDER_DATA_IS_NULL);
        }
        if (item.getGoodsId() == null) {
            log.error("商品ID为空");
            throw new BusinessException(BizErrorCode.ORDER_GOODS_ID_IS_NULL);
        }
        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            log.error("商品数量无效, quantity: {}", item.getQuantity());
            throw new BusinessException(BizErrorCode.ORDER_QUANTITY_INVALID);
        }
    }

    /**
     * 校验库存
     *
     * @param goods    商品信息
     * @param quantity 购买数量
     */
    protected void validateInventory(Goods goods, Integer quantity) {
        if (goods.getInventory() == null || goods.getInventory() < quantity) {
            log.error("商品库存不足, goodsId: {}, 需要数量: {}, 当前库存: {}",
                    goods.getId(), quantity, goods.getInventory());
            throw new BusinessException(BizErrorCode.GOODS_STOCK_INSUFFICIENT);
        }
    }
}