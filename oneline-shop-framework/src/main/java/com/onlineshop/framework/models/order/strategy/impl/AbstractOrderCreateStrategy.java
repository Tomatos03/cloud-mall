package com.onlineshop.framework.models.order.strategy.impl;

import com.onlineshop.framework.models.order.dto.TradeDTO;
import com.onlineshop.framework.models.order.dto.TradeShopDTO;
import com.onlineshop.framework.models.order.dto.TradeShopItemDTO;
import com.onlineshop.framework.models.goods.spu.Goods;
import com.onlineshop.framework.models.order.entity.Order;
import com.onlineshop.framework.models.order.entity.OrderItem;
import com.onlineshop.framework.models.goods.spu.IGoodsService;
import com.onlineshop.framework.models.order.strategy.OrderCreateStrategy;
import com.onlineshop.framework.utils.OrderNoUtil;
import com.onlineshop.framework.utils.context.UserContextHolder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 订单创建策略抽象类
 * 职责：定义订单创建的通用逻辑框架，提取公共方法
 * 模板方法模式：子类通过实现抽象方法来定制具体的行为
 * 
 * 重要说明：
 * ============================================================
 * 本类假设所有数据都已通过 OrderValidateStrategy 的校验：
 * 
 * ✅ 已验证的内容：
 *   - 基本数据完整性（用户、店铺、商品 ID 等不为空）
 *   - 商品存在且已上架
 *   - 商品属于指定的店铺
 *   - 库存充足
 *   - 购物车中存在该商品（仅适用于普通购物车）
 * 
 * ❌ 本类不进行任何校验：
 *   - 不检查商品是否存在
 *   - 不检查库存是否充足
 *   - 不抛出任何业务异常（除非数据严重错误）
 * 
 * 调用流程：
 *   OrderValidateStrategy.validate(tradeDTO)  ← 完成所有校验
 *          ↓
 *   OrderCreateStrategy.buildOrders(tradeDTO) ← 直接构建，无需校验
 * ============================================================
 *
 * @author : Tomatos
 * @date : 2025/12/24
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractOrderCreateStrategy implements OrderCreateStrategy {

    protected final IGoodsService goodsService;

    /**
     * 主入口：构建订单对象和订单明细
     * 
     * 流程：
     * 1. 遍历每个店铺的交易数据
     * 2. 为每个店铺构建一个订单及其订单明细
     * 3. 返回所有构建的订单结果
     * 
     * @param tradeDTO 交易信息（已通过校验）
     * @return 构建好的订单结果列表（按店铺分组）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<OrderBuildResult> buildOrders(TradeDTO tradeDTO) {
        Long userId = UserContextHolder.getUserId();
        List<TradeShopDTO> shopList = tradeDTO.getTradeItems();
        List<OrderBuildResult> resultList = new ArrayList<>(shopList.size());

        // 按店铺分组处理订单
        for (TradeShopDTO shopDTO : shopList) {
            OrderBuildResult result = buildOrderForShop(shopDTO);
            resultList.add(result);
        }

        log.info("订单构建成功, userId: {}, 购物车类型: {}, 店铺数量: {}",
                 userId, getSupportedCartType(), shopList.size());
        return resultList;
    }

    /**
     * 为单个店铺构建订单和订单明细
     * 模板方法：定义订单构建的步骤
     *
     * @param shopDTO 店铺交易数据
     * @return 订单构建结果
     */
    protected OrderBuildResult buildOrderForShop(TradeShopDTO shopDTO) {
        Long storeId = shopDTO.getStoreId();
        List<TradeShopItemDTO> itemList = shopDTO.getTradeShopItemList();

        // 计算订单总价和构建订单明细
        long orderTotalPrice = 0L;
        List<OrderItem> orderItems = new ArrayList<>(itemList.size());

        // 子类可在此之前进行初始化操作
        beforeBuildOrderItems(shopDTO);

        // 构建订单明细
        for (TradeShopItemDTO itemDTO : itemList) {
            // 获取商品信息（由子类实现具体逻辑）
            // 注意：此时商品已通过校验，不需要再次验证
            Goods goods = getGoods(shopDTO, itemDTO);

            // 计算明细小计
            long itemTotalPrice = goods.getPrice() * itemDTO.getQuantity();
            orderTotalPrice += itemTotalPrice;

            // 构建订单明细
            OrderItem orderItem = buildOrderItem(goods, itemDTO, itemTotalPrice);
            orderItems.add(orderItem);

            // 扣减库存
            goodsService.deductInventory(goods.getId(), itemDTO.getQuantity());
            log.info("库存扣减成功, goodsId: {}, 扣减数量: {}",
                     goods.getId(), itemDTO.getQuantity());
        }

        // 子类可在此之后进行清理操作（如从购物车移除商品）
        afterBuildOrderItems(shopDTO, itemList);

        // 构建订单主表
        return buildOrder(storeId, itemList.size(), orderTotalPrice, orderItems);
    }

    /**
     * 在构建订单明细之前的钩子方法
     * 子类可覆盖此方法以执行自定义的初始化逻辑
     * 
     * 使用场景：
     * - 批量加载商品到缓存
     * - 初始化线程本地变量
     * - 预处理数据
     *
     * @param shopDTO 店铺交易数据
     */
    protected void beforeBuildOrderItems(TradeShopDTO shopDTO) {
        // 默认实现为空，子类可选择覆盖
    }

    /**
     * 获取商品信息
     * 子类实现具体的商品获取逻辑
     * 
     * 注意：
     * - 商品已通过校验，肯定存在且已上架
     * - 库存已通过校验，肯定充足
     * - 无需进行任何业务逻辑校验
     *
     * @param shopDTO 店铺交易数据
     * @param itemDTO 商品项DTO
     * @return 商品对象
     */
    protected abstract Goods getGoods(TradeShopDTO shopDTO, TradeShopItemDTO itemDTO);

    /**
     * 构建订单明细项
     *
     * @param goods          商品信息
     * @param itemDTO        商品项DTO
     * @param itemTotalPrice 明细总价
     * @return 订单明细对象
     */
    protected OrderItem buildOrderItem(Goods goods, TradeShopItemDTO itemDTO, long itemTotalPrice) {
        return OrderItem.builder()
                        .goodsId(goods.getId())
                        .goodsName(goods.getName())
                        .goodsImg(goods.getImg())
                        .goodsPrice(goods.getPrice())
                        .quantity(itemDTO.getQuantity())
                        .totalPrice(itemTotalPrice)
                        .build();
    }

    /**
     * 在构建订单明细之后的钩子方法
     * 子类可覆盖此方法以执行自定义的后置逻辑
     * 
     * 使用场景：
     * - 从购物车中移除已购买的商品
     * - 清理缓存
     * - 发送通知
     *
     * @param shopDTO  店铺交易数据
     * @param itemList 商品项列表
     */
    protected void afterBuildOrderItems(TradeShopDTO shopDTO, List<TradeShopItemDTO> itemList) {
        // 默认实现为空，子类可选择覆盖
    }

    /**
     * 构建订单主表
     *
     * @param storeId         店铺ID
     * @param quantity             商品项数量
     * @param orderTotalPrice 订单总价
     * @param orderItems      订单明细列表
     * @return 订单构建结果
     */
    @NonNull
    protected OrderBuildResult buildOrder(
            long storeId,
            int quantity,
            long orderTotalPrice,
            List<OrderItem> orderItems
    ) {
        Long userId = UserContextHolder.getUserId();
        String orderNo = OrderNoUtil.generateOrderNo();
        Order order = Order.builder()
                           .no(orderNo)
                           .userId(userId)
                           .storeId(storeId)
                           .quantity(quantity)
                           .totalPrice(orderTotalPrice)
                           .status(getOrderStatus())
                           .build();
        log.info("店铺订单构建成功, storeId: {}, orderNo: {}", storeId, orderNo);
        return new OrderBuildResult(order, orderItems);
    }

    /**
     * 获取订单状态
     * 子类可覆盖此方法以返回不同的订单状态
     *
     * @return 订单状态码
     */
    protected abstract String getOrderStatus();
}