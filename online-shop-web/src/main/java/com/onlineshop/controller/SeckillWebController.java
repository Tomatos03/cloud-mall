package com.onlineshop.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlineshop.framework.models.seckill.entity.SeckillActivity;
import com.onlineshop.framework.models.seckill.entity.SeckillOrder;
import com.onlineshop.framework.models.seckill.manager.SeckillManager;
import com.onlineshop.framework.models.seckill.service.SeckillActivityService;
import com.onlineshop.framework.models.seckill.service.SeckillOrderService;
import com.onlineshop.framework.models.seckill.vo.SeckillActivityVO;
import com.onlineshop.framework.models.seckill.vo.SeckillOrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 秒杀 Web 控制器（用户端）
 * 处理用户参与秒杀、查询秒杀活动等操作
 *
 * @author Tomatos
 * @date 2025-12-24
 */
@Slf4j
@RestController
@RequestMapping("/web/seckill")
public class SeckillWebController {

    @Autowired
    private SeckillActivityService seckillActivityService;

    @Autowired
    private SeckillOrderService seckillOrderService;

    @Autowired
    private SeckillManager seckillManager;

    /**
     * 获取进行中的秒杀活动列表
     *
     * @param page 页码
     * @param size 每页大小
     * @return 秒杀活动分页数据
     */
    @GetMapping("/activities/ongoing")
    public IPage<SeckillActivityVO> getOngoingSeckillActivities(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info("查询进行中的秒杀活动，页码：{}，每页大小：{}", page, size);

        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<SeckillActivity> wrapper = new LambdaQueryWrapper<>();
        wrapper.le(SeckillActivity::getStartTime, now)
               .ge(SeckillActivity::getEndTime, now)
               .orderByDesc(SeckillActivity::getCreateTime);

        IPage<SeckillActivity> activityPage = seckillActivityService.page(
                new Page<>(page, size), wrapper);

        return convertActivityPage(activityPage);
    }

    /**
     * 将活动分页数据转换为VO分页
     */
    private IPage<SeckillActivityVO> convertActivityPage(IPage<SeckillActivity> page) {
        List<SeckillActivityVO> voList = new ArrayList<>();
        for (SeckillActivity activity : page.getRecords()) {
            voList.add(convertActivityToVO(activity));
        }

        IPage<SeckillActivityVO> voPage = new Page<>();
        voPage.setRecords(voList);
        voPage.setTotal(page.getTotal());
        voPage.setSize(page.getSize());
        voPage.setCurrent(page.getCurrent());

        return voPage;
    }

    /**
     * 将活动实体转换为VO
     */
    private SeckillActivityVO convertActivityToVO(SeckillActivity activity) {
        SeckillActivityVO vo = new SeckillActivityVO();
        vo.setId(activity.getId());
        vo.setProductId(activity.getProductId());
        vo.setStartTime(activity.getStartTime());
        vo.setEndTime(activity.getEndTime());
        vo.setSeckillPrice(activity.getSeckillPrice());
        vo.setStock(activity.getStock());

        // 计算秒杀状态
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartTime())) {
            vo.setStatus(0); // 未开始
        } else if (now.isAfter(activity.getEndTime())) {
            vo.setStatus(2); // 已结束
        } else {
            vo.setStatus(1); // 进行中
        }

        return vo;
    }

    /**
     * 获取即将开始的秒杀活动列表
     *
     * @param page 页码
     * @param size 每页大小
     * @return 秒杀活动分页数据
     */
    @GetMapping("/activities/upcoming")
    public IPage<SeckillActivityVO> getUpcomingSeckillActivities(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info("查询即将开始的秒杀活动，页码：{}，每页大小：{}", page, size);

        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<SeckillActivity> wrapper = new LambdaQueryWrapper<>();
        wrapper.gt(SeckillActivity::getStartTime, now)
               .orderByAsc(SeckillActivity::getStartTime);

        IPage<SeckillActivity> activityPage = seckillActivityService.page(
                new Page<>(page, size), wrapper);

        return convertActivityPage(activityPage);
    }

    /**
     * 获取秒杀活动详情
     *
     * @param seckillId 秒杀活动ID
     * @return 秒杀活动详情
     */
    @GetMapping("/activities/{seckillId}")
    public SeckillActivityVO getSeckillActivityDetail(@PathVariable Long seckillId) {
        log.info("查询秒杀活动详情，秒杀ID：{}", seckillId);

        SeckillActivity activity = seckillActivityService.getById(seckillId);
        if (activity == null) {
            throw new IllegalArgumentException("秒杀活动不存在");
        }

        return convertActivityToVO(activity);
    }

    /**
     * 参与秒杀（核心接口）
     *
     * @param seckillId 秒杀活动ID
     * @param quantity  购买数量
     * @param userId    用户ID（从登录信息获取）
     * @return 秒杀订单ID
     */
    @PostMapping("/participate/{seckillId}")
    public Long participateSeckill(
            @PathVariable Long seckillId,
            @RequestParam(defaultValue = "1") Integer quantity,
            @RequestParam Long userId) {

        log.info("用户 {} 参与秒杀活动 {}，购买数量：{}", userId, seckillId, quantity);

        return seckillManager.participateSeckill(seckillId, userId, quantity);
    }

    /**
     * 获取秒杀订单详情
     *
     * @param seckillOrderId 秒杀订单ID
     * @return 秒杀订单详情
     */
    @GetMapping("/orders/{seckillOrderId}")
    public SeckillOrderVO getSeckillOrderDetail(@PathVariable Long seckillOrderId) {
        log.info("查询秒杀订单详情，订单ID：{}", seckillOrderId);

        SeckillOrder order = seckillOrderService.getById(seckillOrderId);
        if (order == null) {
            throw new IllegalArgumentException("秒杀订单不存在");
        }

        return convertOrderToVO(order);
    }

    /**
     * 将订单实体转换为VO
     */
    private SeckillOrderVO convertOrderToVO(SeckillOrder order) {
        SeckillOrderVO vo = new SeckillOrderVO();
        vo.setId(order.getId());
        vo.setSeckillId(order.getSeckillId());
        vo.setProductId(order.getProductId());
        vo.setOrderId(order.getOrderId());
        vo.setUserId(order.getUserId());
        vo.setSeckillPrice(order.getSeckillPrice());
        vo.setQuantity(order.getQuantity());
        vo.setStatus(order.getStatus());
        vo.setCreateTime(order.getCreateTime());
        vo.setUpdateTime(order.getUpdateTime());

        // 计算总金额
        if (order.getSeckillPrice() != null && order.getQuantity() != null) {
            vo.setTotalAmount(order.getSeckillPrice()
                                   .multiply(new BigDecimal(order.getQuantity())));
        }

        return vo;
    }

    /**
     * 查询用户的秒杀订单列表
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页大小
     * @return 秒杀订单分页数据
     */
    @GetMapping("/orders/user/{userId}")
    public IPage<SeckillOrderVO> getUserSeckillOrders(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info("查询用户 {} 的秒杀订单，页码：{}，每页大小：{}", userId, page, size);

        LambdaQueryWrapper<SeckillOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeckillOrder::getUserId, userId)
               .orderByDesc(SeckillOrder::getCreateTime);

        IPage<SeckillOrder> orderPage = seckillOrderService.page(
                new Page<>(page, size), wrapper);

        return convertOrderPage(orderPage);
    }

    /**
     * 将订单分页数据转换为VO分页
     */
    private IPage<SeckillOrderVO> convertOrderPage(IPage<SeckillOrder> page) {
        List<SeckillOrderVO> voList = new ArrayList<>();
        for (SeckillOrder order : page.getRecords()) {
            voList.add(convertOrderToVO(order));
        }

        IPage<SeckillOrderVO> voPage = new Page<>();
        voPage.setRecords(voList);
        voPage.setTotal(page.getTotal());
        voPage.setSize(page.getSize());
        voPage.setCurrent(page.getCurrent());

        return voPage;
    }

    /**
     * 取消秒杀订单
     *
     * @param seckillOrderId 秒杀订单ID
     * @return 是否成功
     */
    @PostMapping("/orders/{seckillOrderId}/cancel")
    public boolean cancelSeckillOrder(@PathVariable Long seckillOrderId) {
        log.info("取消秒杀订单，订单ID：{}", seckillOrderId);

        SeckillOrder order = seckillOrderService.getById(seckillOrderId);
        if (order == null) {
            throw new IllegalArgumentException("秒杀订单不存在");
        }

        // 只有待支付状态的订单才能取消
        if (!order.getStatus()
                  .equals(0)) {
            throw new IllegalArgumentException("订单状态不允许取消");
        }

        order.setStatus(4); // 4-已取消
        order.setUpdateTime(LocalDateTime.now());
        return seckillOrderService.updateById(order);
    }

    /**
     * 获取秒杀活动的剩余库存
     *
     * @param seckillId 秒杀活动ID
     * @return 剩余库存数量
     */
    @GetMapping("/activities/{seckillId}/stock")
    public Long getSeckillStock(@PathVariable Long seckillId) {
        log.info("查询秒杀活动剩余库存，秒杀ID：{}", seckillId);

        return seckillManager.getRemainingStock(seckillId);
    }
}