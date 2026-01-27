package com.onlineshop.framework.models.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.onlineshop.framework.models.cart.CartType;
import com.onlineshop.framework.models.order.dto.OrderCreateResultDTO;
import com.onlineshop.framework.models.order.dto.OrderQueryDTO;
import com.onlineshop.framework.models.order.dto.TradeDTO;
import com.onlineshop.framework.models.order.entity.Order;
import com.onlineshop.framework.models.order.vo.OrderAggregateVO;
import com.onlineshop.framework.models.order.vo.OrderVO;

/**
 * 订单服务接口
 */
public interface IOrderService extends IService<Order> {
    /**
     * 用户端：分页查询聚合订单（查询父订单和普通订单，并聚合子订单和商品明细）
     * 只能查询当前登录用户的订单
     * 返回聚合视图，包含：
     * - 顶层订单信息（父订单或普通订单）
     * - 所有店铺订单列表（子订单）
     * - 每个店铺的商品明细
     *
     * @param queryDTO 订单查询条件DTO
     * @return 订单聚合视图分页结果
     * @throws com.onlineshop.framework.exception.BusinessException 当查询参数异常时
     */
    IPage<OrderAggregateVO> pageQueryForUser(OrderQueryDTO queryDTO);

    /**
     * 创建订单（包含校验、构建、扣减库存等完整流程）
     *
     * @param tradeDTO 交易数据
     * @param cartType 购物车类型
     * @return 订单创建结果DTO
     */
    OrderCreateResultDTO createOrder(TradeDTO tradeDTO, CartType cartType);

    /**
     * 查询支付状态（模拟接口）
     * 用于前端轮询查询订单支付结果
     * 注意：这是一个模拟接口，实际项目中应对接真实的支付平台
     *
     * @param orderNo 订单号
     * @return 支付是否成功
     */
    boolean queryPaymentStatus(String orderNo);

    /**
     * 根据订单号扣减库存
     * 遍历订单的所有明细，对每个SKU进行库存扣减和销量增加
     * 支持聚合订单：若订单为父订单，则扣减所有子订单明细的库存
     *
     * @param orderNo 订单号
     */
    void deductInventoryByOrderNo(String orderNo);

    /**
     * 根据订单号查询订单明细项列表
     * 一个订单对应一个店铺，一个订单可能有多个订单明细项
     *
     * @param orderNo 订单号
     * @return 订单明细项列表
     * @throws com.onlineshop.framework.exception.BusinessException 当订单不存在时
     */
    OrderAggregateVO getOrderDetailByOrderNo(String orderNo);

    /**
     * 取消订单接口
     * 功能：取消订单，将订单状态从 CREATED 改为 CANCELED
     * 检查订单是否处于 CREATED 状态，只有处于该状态才能取消
     *
     * @param orderNo 订单编号
     * @return 是否成功
     * @throws com.onlineshop.framework.exception.BusinessException 当订单不存在或订单状态不符合要求时
     */
    boolean cancelOrder(String orderNo);

    /**
     * 取消订单接口 - 重载方法（指定用户ID）
     * 功能：取消订单，将订单状态从 CREATED 改为 CANCELED
     * 检查订单是否处于 CREATED 状态，只有处于该状态才能取消
     * 支持管理员或其他场景代理用户操作订单
     *
     * @param orderNo 订单编号
     * @param userId 用户ID
     * @return 是否成功
     * @throws com.onlineshop.framework.exception.BusinessException 当订单不存在或订单状态不符合要求时
     */
    boolean cancelOrder(String orderNo, Long userId);

    /**
     * 超时关闭订单接口
     * @param order 订单实体
     * @return 是否成功
     * @throws com.onlineshop.framework.exception.BusinessException 当订单不存在或订单状态转换不合法时
     */
    boolean closeOrder(Order order);

    /**
     * 用户确认收货接口
     * 功能：确认收货，将订单状态从 SHIPPED 改为 FINISHED
     * 检查订单是否处于 SHIPPED 状态，只有处于该状态才能确认收货
     *
     * @param orderNo 订单编号
     * @return 是否成功
     * @throws com.onlineshop.framework.exception.BusinessException 当订单不存在或订单状态不符合要求时
     */
    boolean finishOrder(String orderNo);

    boolean finishOrder(Order order);

    /**
     * 分页查询所有订单（管理员权限）
     * 管理员可以查询所有订单（普通订单和子订单）
     *
     * @param queryDTO 订单查询条件DTO
     * @return 订单分页结果（OrderVO视图对象）
     */
    IPage<OrderVO> pageQueryForAdmin(OrderQueryDTO queryDTO);

    /**
     * 分页查询自己店铺的订单（商家权限）
     * 商家只能查询自己店铺的订单（普通订单和子订单）
     *
     * @param queryDTO 订单查询条件DTO
     * @return 订单分页结果（OrderVO视图对象）
     */
    IPage<OrderVO> pageQueryForMerchant(OrderQueryDTO queryDTO);

    /**
     * 商家发货接口（商家权限 - 验证店铺归属）
     * 功能：将订单状态从 PAID 改为 SHIPPED
     *
     * @param orderNo 订单编号
     * @return 是否成功
     */
    boolean shipOrderMerchant(String orderNo);

    /**
     * 取消订单接口（商家权限 - 验证店铺归属）
     * 功能：取消订单，将订单状态从 CREATED 改为 CANCELED
     *
     * @param orderNo 订单编号
     * @return 是否成功
     */
    boolean cancelOrderMerchant(String orderNo);

    /**
     * 查询订单评价信息（商家权限 - 验证店铺归属）
     * 商家可以查看用户对订单的评价和自己的回复
     *
     * @param orderNo 订单编号
     * @return 订单信息（包含评价内容）
     */
    OrderVO getOrderCommentMerchant(String orderNo);


                /**
     * 自动收货订单
     * 当订单到达自动收货时间（T + N）时，自动确认收货
     * 将订单状态从 SHIPPED 改为 FINISHED
     *
     * @param order 订单对象，需要包含 autoReceiveTime 字段
     * @return 是否成功
     */
    boolean autoReceiveOrder(Order order);
}
