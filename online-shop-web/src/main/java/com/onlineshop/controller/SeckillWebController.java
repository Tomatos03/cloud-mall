package com.onlineshop.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.onlineshop.framework.application.seckill.SeckillAppService;
import com.onlineshop.framework.application.seckill.vo.SeckillActivityGoodsPageVO;
import com.onlineshop.framework.application.seckill.vo.SeckillGoodsWebDetailVO;
import com.onlineshop.framework.models.seckill.dto.SeckillGoodsParamsDTO;

/**
 * 秒杀 Web 控制器（客户端）
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
    private SeckillAppService seckillAppService;

    /**
     * 获取当前整点秒杀活动及商品列表
     * GET /client/seckill/activities/current-hour/goods/list
     *
     * @param params 秒杀商品查询参数
     * @return 当前整点活动详情与商品分页信息
     */
    @GetMapping("/activities/current-hour/goods/list")
    public SeckillActivityGoodsPageVO getCurrentHourActivityGoods(SeckillGoodsParamsDTO params) {
        log.info("查询当前整点秒杀活动商品，页码：{}，每页大小：{}", params.getPage(), params.getPageSize());
        return seckillAppService.pageHourActivityGoods(params, LocalDateTime.now());
    }

    /**
     * 获取指定时间对应整点秒杀活动及商品列表
     * GET /client/seckill/activities/hour/goods/list
     *
     * @param targetTime 指定时间（ISO格式，如：2026-03-10T14:20:00）
     * @param params     秒杀商品查询参数
     * @return 指定时间对应整点活动详情与商品分页信息
     */
    @GetMapping("/activities/hour/goods/list")
    public SeckillActivityGoodsPageVO getHourActivityGoods(
            @RequestParam("targetTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime targetTime,
            SeckillGoodsParamsDTO params) {
        log.info("查询指定时间整点秒杀活动商品，指定时间：{}，页码：{}，每页大小：{}",
                 targetTime, params.getPage(), params.getPageSize());
        return seckillAppService.pageHourActivityGoods(params, targetTime);
    }

    /**
     * 获取当天全部场次秒杀活动及商品列表
     * GET /client/seckill/activities/today/goods/list
     *
     * @param params 秒杀商品查询参数
     * @return 当天全部场次活动详情与商品分页信息
     */
    @GetMapping("/activities/today/goods/list")
    public List<SeckillActivityGoodsPageVO> getTodayActivityGoods(SeckillGoodsParamsDTO params) {
        LocalDate today = LocalDate.now();
        log.info("查询当天全部场次秒杀活动商品，日期：{}，页码：{}，每页大小：{}",
                 today, params.getPage(), params.getPageSize());
        return seckillAppService.listDayActivityGoods(params, today);
    }

    /**
     * 获取秒杀商品聚合详情（秒杀信息 + SPU详情）
     * GET /client/seckill/product/:id/detail
     *
     * @param id 秒杀商品ID
     * @return 秒杀商品聚合详情
     */
    @GetMapping("/product/{id}/detail")
    public SeckillGoodsWebDetailVO getSeckillGoodsDetail(@PathVariable Long id) {
        log.info("查询秒杀商品聚合详情，秒杀商品ID：{}", id);
        return seckillAppService.getSeckillGoodsWebDetail(id);
    }
}
