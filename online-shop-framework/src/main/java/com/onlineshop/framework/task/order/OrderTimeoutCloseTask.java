package com.onlineshop.framework.task.order;

import com.onlineshop.framework.models.order.entity.Order;
import com.onlineshop.framework.models.order.enums.OrderStatus;
import com.onlineshop.framework.models.order.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单关闭消息补偿定时任务
 * <p>
 * 功能：定时扫描消息发送失败的订单，重新发送订单关闭消息
 * 这是一个兜底方案，确保超时订单最终一定会被关闭
 * <p>
 * 执行频率：每10分钟执行一次
 *
 * @author : Tomatos
 * @date : 2026/01/27
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutCloseTask {
    private final IOrderService orderService;

    @Scheduled(cron = "0 */10 * * * ?")
    public void compensateFailedCloseMessages() {
        List<Order> timeoutOrders = queryTimeoutUnPaidOrders();
        for (Order order : timeoutOrders) {
            try {
                orderService.closeOrder(order);
                log.info("订单关闭消息补偿发送成功，订单ID：{}", order.getId());
            } catch (Exception e) {
                log.error("订单关闭消息补偿发送失败，订单ID：{}", order.getId(), e);
            }
        }
    }

    private List<Order> queryTimeoutUnPaidOrders() {
        return orderService.lambdaQuery()
                           .eq(Order::getStatus, OrderStatus.CREATED) // 状态为CREATED
                           .le(Order::getCreateTime, LocalDateTime.now()
                                                                  .minusMinutes(30)
                           )
                           .list();
    }
}
