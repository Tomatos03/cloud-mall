package com.onlineshop.controller.merchant;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlineshop.framework.models.order.dto.OrderCancelDTO;
import com.onlineshop.framework.models.order.dto.OrderQueryDTO;
import com.onlineshop.framework.models.order.service.IOrderService;
import com.onlineshop.framework.models.order.vo.OrderAggregateVO;
import com.onlineshop.framework.models.order.vo.OrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 商家订单管理 Controller
 * 商家只能管理自己店铺的订单
 */
@RestController
@RequestMapping("/manager/merchant/order")
public class MerchantOrderController {
    @Autowired
    private IOrderService orderService;

    /**
     * 分页查询自己店铺的订单（商家权限）
     * 商家只能查询自己店铺的订单（普通订单和子订单）
     *
     * @param queryDTO 查询条件
     * @return 订单分页结果
     */
    @GetMapping("/page")
    public IPage<OrderVO> pageQueryMerchant(OrderQueryDTO queryDTO) {
        return orderService.pageQueryForMerchant(queryDTO);
    }

    /**
     * 根据订单编号查询订单详细项（商家权限）
     * 返回该订单包含的所有商品详细项列表
     * 需验证订单是否属于当前商家
     *
     * @param orderNo 订单编号
     * @return 订单明细项列表
     */
    @GetMapping("/detail/{orderNo}")
    public OrderAggregateVO getOrderDetail(@PathVariable String orderNo) {
        return orderService.getOrderDetailByOrderNo(orderNo);
    }

    /**
     * 订单发货接口（商家权限）
     * 功能：将订单状态从 PAID 改为 SHIPPED
     * 触发条件：订单状态为 PAID 时显示"发货"按钮
     * 需验证订单是否属于当前商家
     *
     * @param orderNo 订单编号
     * @return 是否成功
     */
    @PostMapping("/ship/{orderNo}")
    public boolean shipOrder(@PathVariable String orderNo) {
        return orderService.shipOrderMerchant(orderNo);
    }

    /**
     * 取消订单接口（商家权限，DTO版本）
     * 功能：取消订单，将订单状态从 CREATED 改为 CANCELED
     * 触发条件：订单状态为 CREATED 时显示"取消订单"按钮
     * 需验证订单是否属于当前商家
     *
     * @param cancelDTO 取消订单请求DTO
     * @return 是否成功
     */
    @PostMapping("/cancel")
    public boolean cancelOrder(@RequestBody OrderCancelDTO cancelDTO) {
        return orderService.cancelOrderMerchant(cancelDTO);
    }

    /**
     * 用户评价订单接口（商家权限 - 查看）
     * 商家可以查看用户对订单的评价和自己的回复
     * 需验证订单是否属于当前商家
     *
     * @param orderNo 订单编号
     * @return 订单信息（包含评价内容）
     */
    @GetMapping("/{orderNo}/comment")
    public OrderVO getOrderComment(@PathVariable String orderNo) {
        return orderService.getOrderCommentMerchant(orderNo);
    }
}