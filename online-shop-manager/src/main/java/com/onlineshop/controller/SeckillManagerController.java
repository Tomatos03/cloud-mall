package com.onlineshop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlineshop.framework.models.seckill.application.SeckillAppService;
import com.onlineshop.framework.models.seckill.dto.SeckillActivityDTO;
import com.onlineshop.framework.models.seckill.dto.SeckillActivityParamsDTO;
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
@RequestMapping("/seckill")
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
     * 更新秒杀活动
     * PUT /admin/seckill/activities/:id
     *
     * @param id  秒杀活动ID
     * @param dto 秒杀活动数据
     * @return 更新后的秒杀活动
     */
    @PutMapping("/activities/{id}")
    public SeckillActivityVO updateSeckillActivity(@PathVariable Long id, @RequestBody SeckillActivityDTO dto) {
        log.info("更新秒杀活动，ID: {}", id);
        return seckillActivityService.updateActivity(id, dto);
    }

    /**
     * 删除秒杀活动
     * DELETE /admin/seckill/activities/:id
     *
     * @param id 秒杀活动ID
     * @return 是否删除成功
     */
    @DeleteMapping("/activities/{id}")
    public boolean deleteSeckillActivity(@PathVariable Long id) {
        log.info("删除秒杀活动，ID: {}", id);
        return seckillActivityService.deleteActivity(id);
    }

    /**
     * 开始秒杀活动
     * POST /admin/seckill/activities/:id/start
     *
     * @param id 秒杀活动ID
     * @return 是否开始成功
     */
    @PostMapping("/activities/{id}/start")
    public boolean startSeckillActivity(@PathVariable Long id) {
        log.info("启动秒杀活动，ID: {}", id);
        return seckillAppService.startSeckillActivity(id);
    }


    /**
     * 查询申请列表
     * GET /admin/seckill/applies/list
     *
     * @param params 查询参数
     * @return 申请列表
     */
    @GetMapping("/applies/list")
    public IPage<?> listApplies(SeckillActivityParamsDTO params) {
        log.info("查询秒杀申请列表，页码: {}, 每页数量: {}", params.getPage(), params.getPageSize());
        return seckillActivityService.listAuditApplies(params);
    }

    /**
     * 获取活动中已审核通过的秒杀商品
     * GET /admin/seckill/goods/list
     *
     * @param params 秒杀商品查询参数
     * @return 已审核通过的秒杀商品列表
     */
    @GetMapping("/goods/list")
    public IPage<?> getApprovedProducts(SeckillGoodsParamsDTO params) {
        log.info("管理员查询活动中已审核通过的商品，活动ID: {}, 页码: {}, 每页数量: {}", 
                params.getActivityId(), params.getPage(), params.getPageSize());
        return seckillActivityService.getApprovedGoodsInActivity(params);
    }
}


