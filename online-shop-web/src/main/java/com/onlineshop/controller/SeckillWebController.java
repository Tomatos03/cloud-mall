package com.onlineshop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlineshop.framework.models.seckill.dto.SeckillActivityParamsDTO;
import com.onlineshop.framework.models.seckill.dto.SeckillGoodsDTO;
import com.onlineshop.framework.models.seckill.dto.SeckillGoodsParamsDTO;
import com.onlineshop.framework.models.seckill.entity.SeckillActivity;
import com.onlineshop.framework.models.seckill.application.SeckillAppService;
import com.onlineshop.framework.models.seckill.service.SeckillActivityService;
import com.onlineshop.framework.models.seckill.service.SeckillGoodsService;
import com.onlineshop.framework.models.seckill.service.SeckillOrderService;
import com.onlineshop.framework.models.seckill.vo.SeckillActivityVO;
import com.onlineshop.framework.models.seckill.vo.SeckillOrderVO;
import com.onlineshop.framework.utils.AuthUserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class SeckillWebController {

    @Autowired
    private SeckillAppService seckillAppService;
    @Autowired
    private SeckillActivityService seckillActivityService;
    @Autowired
    private SeckillOrderService seckillOrderService;
    @Autowired
    private SeckillGoodsService seckillGoodsService;

    // ==================== 秒杀活动查询接口 ====================

    /**
     * 获取秒杀活动列表
     * GET /client/seckill/activities/list
     *
     * @param params 查询参数
     * @return 秒杀活动分页数据
     */
    @GetMapping("/activities/list")
    public IPage<SeckillActivityVO> getSeckillActivities(SeckillActivityParamsDTO params) {
        log.info("查询秒杀活动列表，页码：{}，每页大小：{}", params.getPage(), params.getPageSize());
        return seckillActivityService.listActivities(params);
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
     * GET /client/seckill/products/list
     *
     * @param params 秒杀商品查询参数
     * @return 活动内的秒杀商品列表
     */
    @GetMapping("/products/list")
    public IPage<SeckillGoodsDTO> getActivityProducts(SeckillGoodsParamsDTO params) {
        log.info("查询活动商品列表，活动ID：{}", params.getActivityId());

        // 验证活动存在
        SeckillActivity activity = seckillActivityService.getById(params.getActivityId());
        if (activity == null) {
            throw new com.onlineshop.framework.exception.BizException(
                com.onlineshop.framework.common.enums.BizErrorCode.SECKILL_ACTIVITY_NOT_EXIST);
        }

        // 通过service获取该活动的所有秒杀商品
        return seckillGoodsService.getActivityProducts(params);
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
     * POST /client/seckill/participate/:seckillGoodsId
     *
     * @param seckillGoodsId 秒杀商品ID
     * @param quantity       购买数量（默认1）
     * @return 秒杀订单ID
     */
    @PostMapping("/participate/{seckillGoodsId}")
    public Long participateSeckill(
            @PathVariable Long seckillGoodsId,
            @RequestParam(defaultValue = "1") Integer quantity) {

        // 获取当前登录用户ID
        Long userId = AuthUserUtils.getUserId();

        log.info("用户 {} 参与秒杀商品 {}，购买数量：{}", userId, seckillGoodsId, quantity);

        return seckillAppService.participateSeckill(seckillGoodsId, userId, quantity).getOrderId();
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
     * GET /client/seckill/orders/user/:userId/list
     *
     * @param userId 用户ID
     * @param params 查询参数
     * @return 秒杀订单分页数据
     */
    @GetMapping("/orders/user/{userId}/list")
    public IPage<SeckillOrderVO> getUserSeckillOrders(
            @PathVariable Long userId,
            SeckillActivityParamsDTO params) {

        log.info("查询用户 {} 的秒杀订单，页码：{}，每页大小：{}", userId, params.getPage(), params.getPageSize());
        return seckillOrderService.getUserSeckillOrders(userId, params.getPage(), params.getPageSize());
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
