package com.onlineshop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlineshop.framework.models.seckill.dto.SeckillActivityDTO;
import com.onlineshop.framework.models.seckill.service.SeckillActivityService;
import com.onlineshop.framework.models.seckill.vo.SeckillActivityVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
@RequiredArgsConstructor
public class SeckillManagerController {
    private final SeckillActivityService seckillActivityService;

    /**
     * 创建秒杀活动
     * POST /admin/seckill/activities
     *
     * @param dto 秒杀活动数据
     * @return 创建成功的秒杀活动
     */
    @PostMapping("/activities")
    public SeckillActivityVO createSeckillActivity(@RequestBody SeckillActivityDTO dto) {
        log.info("创建秒杀活动，产品ID: {}", dto.getProductId());
        return seckillActivityService.createActivity(dto);
    }

    /**
     * 获取秒杀活动列表
     * GET /admin/seckill/activities
     *
     * @param pageNum  页码（默认1）
     * @param pageSize 每页数量（默认10）
     * @return 秒杀活动列表
     */
    @GetMapping("/activities")
    public IPage<SeckillActivityVO> listSeckillActivities(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        log.info("查询秒杀活动列表，页码: {}, 每页数量: {}", pageNum, pageSize);
        return seckillActivityService.listActivities(pageNum, pageSize);
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
        log.info("开始秒杀活动，ID: {}", id);
        return seckillActivityService.startActivity(id);
    }

    // ==================== 申请审核管理接口 ====================

    /**
     * 查询申请列表
     * GET /admin/seckill/applies
     *
     * @param pageNum  页码（默认1）
     * @param pageSize 每页数量（默认10）
     * @return 申请列表
     */
    @GetMapping("/applies")
    public IPage<?> listApplies(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        log.info("查询秒杀申请列表，页码: {}, 每页数量: {}", pageNum, pageSize);
        return seckillActivityService.listAuditApplies(pageNum, pageSize);
    }

    /**
     * 通过申请
     * POST /admin/seckill/applies/:id/approve
     *
     * @param id 申请ID（审核ID）
     * @return 是否通过成功
     */
    @PostMapping("/applies/{id}/approve")
    public boolean approveApply(@PathVariable Long id) {
        log.info("通过秒杀申请，申请ID: {}", id);
        return seckillActivityService.approveApply(id);
    }

    /**
     * 驳回申请
     * POST /admin/seckill/applies/:id/reject
     *
     * @param id        申请ID（审核ID）
     * @param reasonMap 驳回原因
     * @return 是否驳回成功
     */
    @PostMapping("/applies/{id}/reject")
    public boolean rejectApply(@PathVariable Long id, @RequestBody Map<String, String> reasonMap) {
        log.info("驳回秒杀申请，申请ID: {}", id);
        String reason = reasonMap.get("reason");
        return seckillActivityService.rejectApply(id, reason);
    }
}
