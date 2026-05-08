package com.onlineshop.framework.task.order;

import com.onlineshop.framework.application.order.IOrderAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 订单自动收货定时任务
 *
 * 功能：定时扫描已发货的订单，检查是否到达自动收货时间 (T + N)
 * 如果到达自动收货时间，则自动将订单状态改为已完成 (FINISHED)
 *
 * 场景说明：
 * - 买家下单：2026-01-05 21:00
 * - 商家发货：2026-01-05 22:00，订单进入 SHIPPED 状态
 * - 自动收货截止时间：2026-01-19 23:59 (发货后14天)
 * - 定时任务每天凌晨2点执行，到达截止时间后自动确认收货
 *
 * 执行频率：每天凌晨2点执行一次
 *
 * @author : Tomatos
 * @date : 2026/01/27
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderAutoReceiveTask {

    private final IOrderAppService orderAppService;

    /**
     * 定时扫描并自动确认收货
     *
     * 工作流程：
     * 1. 查询所有状态为 SHIPPED (待收货) 的订单
     * 2. 逐个检查订单的 autoReceiveTime 是否已过期
     * 3. 对于已过期的订单，调用 autoReceiveOrder 自动确认收货
     * 4. 记录自动收货的订单数量
     *
     * 执行周期：每天凌晨2点执行一次 (0 0 2 * * ?)
     * 说明：可根据实际业务调整执行时间
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void autoReceiveOrders() {
        try {
            log.info("开始执行订单自动收货定时任务");
            orderAppService.autoReceiveShippedOrders();
            log.info("订单自动收货定时任务完成");
        } catch (Exception e) {
            log.error("订单自动收货定时任务执行异常", e);
        }
    }
}
