package com.onlineshop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlineshop.framework.models.order.application.IOrderAppService;
import com.onlineshop.framework.models.order.dto.OrderCancelDTO;
import com.onlineshop.framework.models.order.dto.OrderParamsDTO;
import com.onlineshop.framework.models.order.vo.OrderAggregateVO;
import com.onlineshop.framework.models.order.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/2/11
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderMerchantController {
    private final IOrderAppService orderAppService;

    /**
     * 分页查询订单列表
     * GET /merchant/orders/page
     *
     * @param paramsDTO 查询参数：page, pageSize, status等
     * @return 分页结果
     */
    @GetMapping("/page")
    public IPage<OrderVO> pageOrders(OrderParamsDTO paramsDTO) {
        return orderAppService.pageQueryOrdersForAdmin(paramsDTO);
    }

    /**
     * 获取订单详情
     * GET /merchant/orders/{orderNo}
     *
     * @param orderNo 订单编号
     * @return 订单详情
     */
    @GetMapping("/{orderNo}")
    public OrderAggregateVO getOrderDetail(@PathVariable String orderNo) {
        return orderAppService.queryOrderDetail(orderNo);
    }

    /**
     * 取消订单
     * PUT /merchant/orders/{orderNo}/cancel
     *
     * @param orderNo 订单编号
     * @param cancelDTO 取消参数
     * @return 操作是否成功
     */
    @PutMapping("/{orderNo}/cancel")
    public void cancelOrder(@PathVariable String orderNo, @RequestBody OrderCancelDTO cancelDTO) {
        cancelDTO.setOrderNo(orderNo);
        orderAppService.cancelOrder(cancelDTO);
    }

    /**
     * 发货
     * PUT /merchant/orders/{orderNo}/ship
     *
     * @param orderNo 订单编号
     * @return 操作是否成功
     */
    @PutMapping("/{orderNo}/ship")
    public boolean shipOrder(@PathVariable String orderNo) {
        return orderAppService.shipOrder(orderNo);
    }
}
