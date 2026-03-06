package com.onlineshop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlineshop.framework.models.seckill.dto.SeckillGoodsDTO;
import com.onlineshop.framework.models.seckill.entity.SeckillActivity;
import com.onlineshop.framework.models.seckill.service.SeckillActivityService;
import com.onlineshop.framework.models.seckill.service.SeckillGoodsService;
import com.onlineshop.framework.models.seckill.service.SeckillOrderService;
import com.onlineshop.framework.models.seckill.vo.SeckillActivityVO;
import com.onlineshop.framework.models.seckill.vo.SeckillOrderVO;
import com.onlineshop.framework.utils.AuthUserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 秒杀 Web 控制器（客户端）
 * 处理用户参与秒杀、查询秒杀活动等操作
 *
 * @author Tomatos
 * @date 2025-12-24
 */
@Slf4j
@RestController
@RequestMapping("/seckill")
@RequiredArgsConstructor
public class SeckillWebController {

    private final SeckillActivityService seckillActivityService;
    private final SeckillOrderService seckillOrderService;
    private final SeckillGoodsService seckillGoodsService;

    // ==================== 秒杀活动查询接口 ====================

    /**
     * 获取秒杀活动列表
     * GET /client/seckill/activities
     *
     * @param pageNum  页码（默认1）
     * @param pageSize 每页数量（默认10）
     * @return 秒杀活动分页数据
     */
    @GetMapping("/activities")
    public IPage<SeckillActivityVO> getSeckillActivities(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        log.info("查询秒杀活动列表，页码：{}，每页大小：{}", pageNum, pageSize);
        return seckillActivityService.listActivities(pageNum, pageSize);
    }

    /**
     * 获取活动详情
     * GET /client/seckill/activities/:id
     *
     * @param id 秒杀活动ID
     * @return 秒杀活动详情
     */
    @GetMapping("/activities/{id}")
    public SeckillActivityVO getSeckillActivityDetail(@PathVariable Long id) {
        log.info("查询秒杀活动详情，秒杀ID：{}", id);
        return seckillActivityService.getSeckillActivityVO(id);
    }

    // ==================== 秒杀参与接口 ====================

    /**
     * 获取活动商品列表
     * GET /client/seckill/products/:activityId
     *
     * @param activityId 秒杀活动ID
     * @param pageNum    页码（默认1）
     * @param pageSize   每页数量（默认10）
     * @return 活动内的秒杀商品列表
     */
    @GetMapping("/products/{activityId}")
    public IPage<SeckillGoodsDTO> getActivityProducts(
            @PathVariable Long activityId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        log.info("查询活动商品列表，活动ID：{}", activityId);

        // 验证活动存在
        SeckillActivity activity = seckillActivityService.getById(activityId);
        if (activity == null) {
            throw new com.onlineshop.framework.exception.BizException(
                com.onlineshop.framework.common.enums.BizErrorCode.SECKILL_ACTIVITY_NOT_EXIST);
        }

        // 通过service获取该活动的所有秒杀商品
        return seckillGoodsService.getActivityProducts(activityId, pageNum, pageSize);
    }

    /**
     * 获取秒杀商品详情
     * GET /client/seckill/product/:id
     *
     * @param id 秒杀商品ID
     * @return 秒杀商品详情
     */
    @GetMapping("/product/{id}")
    public SeckillGoodsDTO getSeckillProductDetail(@PathVariable Long id) {
        log.info("查询秒杀商品详情，商品ID：{}", id);

        // 通过service获取秒杀商品详情
        SeckillGoodsDTO result = seckillGoodsService.getSeckillProductDetail(id);
        if (result == null) {
            throw new com.onlineshop.framework.exception.BizException(
                com.onlineshop.framework.common.enums.BizErrorCode.SECKILL_ACTIVITY_NOT_EXIST);
        }

        return result;
    }

    /**
     * 参与秒杀（核心接口）
     * POST /client/seckill/participate/:seckillId
     *
     * @param seckillId 秒杀活动ID
     * @param quantity  购买数量（默认1）
     * @return 秒杀订单ID
     */
    @PostMapping("/participate/{seckillId}")
    public Long participateSeckill(
            @PathVariable Long seckillId,
            @RequestParam(defaultValue = "1") Integer quantity) {

        // 获取当前登录用户ID
        Long userId = AuthUserUtils.getUserId();

        log.info("用户 {} 参与秒杀活动 {}，购买数量：{}", userId, seckillId, quantity);

        return seckillOrderService.participateSeckill(seckillId, userId, quantity).getId();
    }

    // ==================== 秒杀订单操作接口 ====================

    /**
     * 获取秒杀订单详情
     * GET /client/seckill/orders/:orderId
     *
     * @param orderId 秒杀订单ID
     * @return 秒杀订单详情
     */
    @GetMapping("/orders/{orderId}")
    public SeckillOrderVO getSeckillOrderDetail(@PathVariable Long orderId) {
        log.info("查询秒杀订单详情，订单ID：{}", orderId);
        return seckillOrderService.getSeckillOrderVO(orderId);
    }

    /**
     * 查询用户的秒杀订单列表
     * GET /client/seckill/orders/user/:userId
     *
     * @param pageNum  页码（默认1）
     * @param pageSize 每页数量（默认10）
     * @return 秒杀订单分页数据
     */
    @GetMapping("/orders/user/{userId}")
    public IPage<SeckillOrderVO> getUserSeckillOrders(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        log.info("查询用户 {} 的秒杀订单，页码：{}，每页大小：{}", userId, pageNum, pageSize);
        return seckillOrderService.getUserSeckillOrders(userId, pageNum, pageSize);
    }

    /**
     * 取消秒杀订单
     * POST /client/seckill/orders/:orderId/cancel
     *
     * @param orderId 秒杀订单ID
     * @return 是否取消成功
     */
    @PostMapping("/orders/{orderId}/cancel")
    public boolean cancelSeckillOrder(@PathVariable Long orderId) {
        log.info("取消秒杀订单，订单ID：{}", orderId);
        return seckillOrderService.cancelSeckillOrderByUser(orderId);
    }
}
