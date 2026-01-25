package com.onlineshop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlineshop.framework.models.cart.CartType;
import com.onlineshop.framework.models.order.dto.OrderCreateResultDTO;
import com.onlineshop.framework.models.order.dto.OrderQueryDTO;
import com.onlineshop.framework.models.order.dto.TradeDTO;
import com.onlineshop.framework.models.order.service.IOrderService;
import com.onlineshop.framework.models.order.vo.OrderAggregateVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 订单 Web 控制器
 * 处理用户端订单相关请求，所有操作基于当前登录用户
 *
 * @author Tomatos
 * @date 2025/12/23
 */
@RestController
@RequestMapping("/web/order")
public class OrderWebController {
    @Autowired
    private IOrderService orderService;

    /**
     * 创建订单
     *
     * @param tradeDTO 交易数据
     * @param cartType 购物车类型（direct-直接购买，cart-购物车购买）
     * @return 订单创建结果
     */
    @PostMapping("/create/{cartType}")
    public OrderCreateResultDTO createOrder(@RequestBody TradeDTO tradeDTO, @PathVariable String cartType) {
        return orderService.createOrder(tradeDTO, CartType.of(cartType));
    }

    /**
     * 用户端：分页查询聚合订单
     * 只查询当前用户的订单（父订单和普通订单）
     * 返回聚合视图，包含：
     * - 订单基本信息
     * - 所有店铺的子订单列表
     * - 每个店铺的商品明细
     *
     * @param queryDTO 订单查询条件
     * @return 订单聚合视图分页数据
     */
    @GetMapping("/page")
    public IPage<OrderAggregateVO> pageQuery(OrderQueryDTO queryDTO) {
        return orderService.pageQueryForUser(queryDTO);
    }

    /**
     * 查询支付状态
     *
     * @param orderNo 订单号
     * @return 支付是否成功
     */
    @GetMapping("/payment/status")
    public boolean checkPaymentStatus(@RequestParam String orderNo) {
        return orderService.queryPaymentStatus(orderNo);
    }

    /**
     * 用户取消订单接口
     * 功能：取消订单，将订单状态从 CREATED 改为 CANCELED
     * 触发条件：订单状态为 CREATED 时显示"取消订单"按钮
     *
     * @param orderNo 订单编号
     * @return 是否成功
     */
    @PostMapping("/cancel/{orderNo}")
    public boolean cancelOrder(@PathVariable String orderNo) {
        return orderService.cancelOrder(orderNo);
    }

    /**
     * 用户确认收货接口
     * 功能：确认收货，将订单状态从 SHIPPED 改为 FINISHED
     * 触发条件：订单状态为 SHIPPED 时显示"确认收货"按钮
     *
     * @param orderNo 订单编号
     * @return 是否成功
     */
    @PostMapping("/confirm/{orderNo}")
    public boolean confirmReceipt(@PathVariable String orderNo) {
        return orderService.finishOrder(orderNo);
    }
}
