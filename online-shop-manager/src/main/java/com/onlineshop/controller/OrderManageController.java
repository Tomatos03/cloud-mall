package com.onlineshop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlineshop.framework.models.order.dto.OrderCancelDTO;
import com.onlineshop.framework.models.order.dto.OrderParamsDTO;
import com.onlineshop.framework.models.order.entity.Order;
import com.onlineshop.framework.models.order.service.IOrderService;
import com.onlineshop.framework.models.order.vo.OrderAggregateVO;
import com.onlineshop.framework.models.order.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 订单管理控制器
 * 合并自 admin/AdminOrderController + merchant/MerchantOrderController
 */
@Slf4j
@RestController
@RequestMapping("/manage/order")
@PreAuthorize("hasAuthority('order:view')")
public class OrderManageController {
    @Autowired
    private IOrderService orderService;

    /**
     * 根据订单ID获取订单详情
     * 来自 admin/AdminOrderController
     *
     * @param orderId 订单ID
     * @return 订单信息
     */
    @GetMapping("/{orderId}")
    public Order getOrder(@PathVariable Long orderId) {
        return orderService.getById(orderId);
    }

    /**
     * 分页查询订单
     * 来自 admin/AdminOrderController + merchant/MerchantOrderController
     * Admin权限：查询所有订单
     * Merchant权限：仅查询自己店铺的订单
     *
     * @param queryDTO 查询条件
     * @return 订单分页结果
     */
    @GetMapping("/page")
    public IPage<OrderVO> pageQuery(OrderParamsDTO queryDTO) {
        return orderService.pageQuery(queryDTO);
    }

    /**
     * 根据订单编号查询订单详细项
     * 来自 admin/AdminOrderController + merchant/MerchantOrderController
     * 返回该订单包含的所有商品详细项列表
     *
     * @param orderNo 订单编号
     * @return 订单明细项列表
     */
    @GetMapping("/detail/{orderNo}")
    public OrderAggregateVO getOrderDetail(@PathVariable String orderNo) {
        return orderService.getOrderDetailByOrderNo(orderNo);
    }

    /**
     * 取消订单
     * 来自 admin/AdminOrderController + merchant/MerchantOrderController
     *
     * @param cancelDTO 取消订单请求DTO
     * @return 是否成功
     */
    @PostMapping("/cancel")
    @PreAuthorize("hasAuthority('order:edit')")
    public boolean cancelOrder(@RequestBody OrderCancelDTO cancelDTO) {
        return orderService.cancelOrder(cancelDTO);
    }

    /**
     * 订单发货
     * 来自 merchant/MerchantOrderController
     * 功能：将订单状态从 PAID 改为 SHIPPED
     * 触发条件：订单状态为 PAID 时显示"发货"按钮
     * Merchant权限：需验证订单是否属于当前商家
     *
     * @param orderNo 订单编号
     * @return 是否成功
     */
    @PostMapping("/ship/{orderNo}")
    @PreAuthorize("hasAuthority('order:edit')")
    public boolean shipOrder(@PathVariable String orderNo) {
        return orderService.shipOrderMerchant(orderNo);
    }

    /**
     * 获取订单评价
     * 来自 merchant/MerchantOrderController
     * 商家可以查看用户对订单的评价和自己的回复
     * Merchant权限：需验证订单是否属于当前商家
     *
     * @param orderNo 订单编号
     * @return 订单信息（包含评价内容）
     */
    @GetMapping("/{orderNo}/comment")
    public OrderVO getOrderComment(@PathVariable String orderNo) {
        return orderService.getOrderCommentMerchant(orderNo);
    }
}