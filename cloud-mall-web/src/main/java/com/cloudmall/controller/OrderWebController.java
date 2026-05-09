package com.cloudmall.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cloudmall.framework.models.cart.PurchaseMode;
import com.cloudmall.framework.models.order.dto.OrderCancelDTO;
import com.cloudmall.framework.models.order.dto.OrderCreateResultDTO;
import com.cloudmall.framework.models.order.dto.OrderParamsDTO;
import com.cloudmall.framework.models.order.dto.TradeDTO;
import com.cloudmall.framework.application.order.IOrderAppService;
import com.cloudmall.framework.models.order.vo.OrderAggregateVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单 Web 控制器
 * 处理用户端订单相关请求，所有操作基于当前登录用户
 *
 * @author Tomatos
 * @date 2025-12-23
 */
@RestController
@RequestMapping("/web/order")
public class OrderWebController {
    @Autowired
    private IOrderAppService orderAppService;

    /**
     * 创建订单
     *
     * @param tradeDTO 交易数据
     * @param cartType 购物车类型（direct-直接购买，cart-购物车购买）
     * @return 订单创建结果
     */
    @PostMapping("/create/{cartType}")
    public OrderCreateResultDTO createOrder(@RequestBody TradeDTO tradeDTO, @PathVariable String cartType) {
        return orderAppService.createOrder(tradeDTO, PurchaseMode.of(cartType));
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
    public IPage<OrderAggregateVO> pageQuery(OrderParamsDTO queryDTO) {
        return orderAppService.pageQueryOrdersForClient(queryDTO);
    }

    /**
     * 查询支付状态
     *
     * @param orderNo 订单号
     * @return 支付是否成功
     */
    @GetMapping("/payment/status")
    public void checkPaymentStatus(@RequestParam String orderNo) throws InterruptedException {
        orderAppService.queryPaymentStatus(orderNo);
    }

    /**
     * 用户取消订单接口
     * 功能：取消订单，将订单状态从 CREATED 改为 CANCELED
     * 触发条件：订单状态为 CREATED 时显示"取消订单"按钮
     *
     * @param cancelDTO 取消订单请求DTO
     * @return 是否成功
     */
    @PostMapping("/cancel")
    public void cancelOrder(@RequestBody OrderCancelDTO cancelDTO) {
        orderAppService.cancelOrder(cancelDTO);
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
    public void confirmReceipt(@PathVariable String orderNo) {
        orderAppService.finishOrder(orderNo);
    }
}
