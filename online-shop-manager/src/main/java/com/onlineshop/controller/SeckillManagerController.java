package com.onlineshop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlineshop.framework.application.seckill.SeckillAppService;
import com.onlineshop.framework.models.seckill.dto.SeckillActivityDTO;
import com.onlineshop.framework.models.seckill.dto.SeckillActivityParamsDTO;
import com.onlineshop.framework.models.seckill.dto.SeckillGoodsDTO;
import com.onlineshop.framework.models.seckill.dto.SeckillGoodsParamsDTO;
import com.onlineshop.framework.models.seckill.service.SeckillActivityService;
import com.onlineshop.framework.models.seckill.vo.SeckillActivityVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 秒杀管理端控制器
 * 处理后台秒杀活动管理相关请求
 * 
 * 管理员可以：
 * - 创建、查询、更新、删除秒杀活动
 * - 查询和审核商家申请
 * - 开始活动
 *
 * @author Tomatos
 * @date 2025-01-10
 */
@Slf4j
@RestController
@RequestMapping("/manager/seckill")
public class SeckillManagerController {
    @Autowired
    private SeckillAppService seckillAppService;
    @Autowired
    private SeckillActivityService seckillActivityService;

    /**
     * 创建秒杀活动
     * POST /admin/seckill/activities
     *
     * @param dto 秒杀活动数据
     * @return 创建成功的秒杀活动
     */
    @PostMapping("/activities")
    public SeckillActivityVO createSeckillActivity(@RequestBody SeckillActivityDTO dto) {
        log.info("创建秒杀活动，活动名称: {}", dto.getName());
        return seckillActivityService.createActivity(dto);
    }

    /**
     * 获取秒杀活动列表
     * GET /admin/seckill/activities/list
     *
     * @param params 秒杀活动查询参数
     * @return 秒杀活动列表
     */
    @GetMapping("/activities/list")
    public IPage<SeckillActivityVO> listSeckillActivities(SeckillActivityParamsDTO params) {
        log.info("查询秒杀活动列表，页码: {}, 每页数量: {}", params.getPage(), params.getPageSize());
        return seckillActivityService.listActivities(params);
    }

    /**
     * 获取秒杀活动详情
     * GET /admin/seckill/activities/:id
     *
     * @param id 秒杀活动ID
     * @return 秒杀活动详情
     */
    @GetMapping("/activities/{id}")
    public SeckillActivityVO getSeckillActivity(@PathVariable Long id) {
        log.info("查询秒杀活动详情，ID: {}", id);
        return seckillActivityService.getSeckillActivityVO(id);
    }

    /**
     * 获取活动中的已通过商品（管理端）
     * GET /admin/seckill/activities/{id}/goods
     *
     * @param id 活动ID
     * @param params 秒杀商品分页参数
     * @return 活动商品分页列表
     */
    @GetMapping("/activities/{id}/goods")
    public IPage<SeckillGoodsDTO> getActivityGoods(@PathVariable Long id, SeckillGoodsParamsDTO params) {
        log.info("管理端查询活动商品，活动ID: {}, 页码: {}, 每页数量: {}", id, params.getPage(), params.getPageSize());
        params.setActivityId(id);
        return seckillAppService.pageSeckillActivityGoods(params);
    }
}
