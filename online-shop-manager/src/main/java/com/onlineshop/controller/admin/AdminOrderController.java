package com.onlineshop.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlineshop.framework.models.order.dto.OrderQueryDTO;
import com.onlineshop.framework.models.order.entity.Order;
import com.onlineshop.framework.models.order.service.IOrderService;
import com.onlineshop.framework.models.order.vo.OrderAggregateVO;
import com.onlineshop.framework.models.order.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员订单管理 Controller
 * 管理员拥有最高权限，可以查看和管理所有订单
 */
@Slf4j
@RestController
@RequestMapping("/manager/admin/order")
public class AdminOrderController {
    @Autowired
    private IOrderService orderService;

    /**
     * 根据订单ID获取订单详情（管理员权限）
     * 
     * @param orderId 订单ID
     * @return 订单信息
     */
    @GetMapping("/{orderId}")
    public Order getOrder(@PathVariable Long orderId) {
        return orderService.getById(orderId);
    }

    /**
     * 分页查询所有订单（管理员权限）
     * 管理员可以查询所有订单（普通订单和子订单）
     *
     * @param queryDTO 查询条件
     * @return 订单分页结果
     */
    @GetMapping("/page")
    public IPage<OrderVO> pageQueryAdmin(OrderQueryDTO queryDTO) {
        return orderService.pageQueryForAdmin(queryDTO);
    }

    /**
     * 根据订单编号查询订单详细项（管理员权限）
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
     * 取消订单接口（管理员权限）
     * 功能：取消订单，将订单状态从 CREATED 改为 CANCELED
     * 触发条件：订单状态为 CREATED 时显示"取消订单"按钮
     *
     * @param orderNo 订单编号（URL路径参数）
     * @return 是否成功
     */
    @PostMapping("/cancel/{orderNo}")
    public boolean cancelOrder(@PathVariable String orderNo) {
        return orderService.cancelOrder(orderNo);
    }

    /**
     * 查询指定店铺的订单（管理员权限）
     * 
     * @param storeId 店铺ID
     * @param queryDTO 查询条件
     * @return 订单分页结果
     */
    @GetMapping("/store/{storeId}/page")
    public IPage<OrderVO> pageQueryByStore(@PathVariable Long storeId, OrderQueryDTO queryDTO) {
        queryDTO.setStoreId(storeId);
        return orderService.pageQueryForAdmin(queryDTO);
    }
}