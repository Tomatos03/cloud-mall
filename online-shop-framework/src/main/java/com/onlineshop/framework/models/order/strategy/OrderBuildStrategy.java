package com.onlineshop.framework.models.order.strategy;

import com.onlineshop.framework.models.address.Address;
import com.onlineshop.framework.models.order.dto.TradeDTO;
import com.onlineshop.framework.models.order.entity.Order;
import com.onlineshop.framework.models.order.entity.OrderItem;
import com.onlineshop.framework.models.cart.CartType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 订单创建策略接口
 * 职责：构建订单对象和订单明细，执行相关业务逻辑（如扣减库存），但不负责保存订单
 *
 * @author : Tomatos
 * @date : 2025/12/24
 */
public interface OrderBuildStrategy {
    
    /**
     * 构建订单对象和订单明细
     * 策略负责：
     * 1. 获取商品和店铺信息
     * 2. 校验库存
     * 3. 计算订单金额
     * 4. 构建订单对象和订单明细
     * 5. 执行库存扣减等业务操作
     * <p>
     * 不负责：
     * - 保存订单到数据库（由 OrderService 负责）
     *
     * @param tradeDTO 交易信息
     * @param address
     * @return 构建好的订单结果列表（按店铺分组）
     */
    OrderBuildResult buildOrders(TradeDTO tradeDTO, Address address);
    
    /**
     * 获取支持的购物车类型
     *
     * @return 购物车类型
     */
    CartType getSupportedCartType();
    
    /**
     * 订单构建结果
     * 包含一个订单及其所有订单明细
     */
    @Data
    @AllArgsConstructor
    class RawOrderBuild {
        /**
         * 订单信息
         */
        private Order order;
        
        /**
         * 订单明细列表
         */
        private List<OrderItem> orderItems;
    }

    @Data
    @AllArgsConstructor
    class OrderBuildResult {
        /**
         * 实际支付订单
         */
        private Order payOrder;

        /**
         * 订单列表
         */
        private List<RawOrderBuild> subOrders;
    }
}