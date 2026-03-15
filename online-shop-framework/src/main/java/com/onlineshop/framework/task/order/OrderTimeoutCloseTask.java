package com.onlineshop.framework.task.order;

import com.onlineshop.framework.models.order.application.IOrderAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
    private final IOrderAppService orderAppService;

    @Scheduled(cron = "0 */10 * * * ?")
    public void compensateFailedCloseMessages() {
        int closedCount = orderAppService.closeTimeoutOrders();
        log.info("订单关闭消息补偿任务完成，本次成功关闭 {} 个订单", closedCount);
    }
}
