package com.onlineshop.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.exception.BizException;
import com.onlineshop.framework.models.audit.application.IAuditAppService;
import com.onlineshop.framework.models.audit.dto.AuditDecisionDTO;
import com.onlineshop.framework.models.audit.dto.AuditParamsDTO;
import com.onlineshop.framework.models.audit.service.IAuditService;
import com.onlineshop.framework.models.seckill.dto.SeckillActivityDTO;
import com.onlineshop.framework.models.seckill.entity.SeckillActivity;
import com.onlineshop.framework.models.seckill.manager.SeckillManager;
import com.onlineshop.framework.models.seckill.service.SeckillActivityService;
import com.onlineshop.framework.models.seckill.vo.SeckillActivityVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

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
@RequestMapping("/admin/seckill")
@RequiredArgsConstructor
public class SeckillManagerController {

    private final SeckillActivityService seckillActivityService;
    private final SeckillManager seckillManager;
    private final IAuditAppService auditAppService;
    private final IAuditService auditService;

    // ==================== 秒杀活动管理接口 ====================

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

        SeckillActivity activity = new SeckillActivity();
        BeanUtils.copyProperties(dto, activity);
        activity.setCreateTime(LocalDateTime.now());
        activity.setUpdateTime(LocalDateTime.now());

        seckillActivityService.save(activity);

        // 初始化Redis库存
        seckillManager.initializeStock(activity.getId());

        return convertToVO(activity);
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

        Page<SeckillActivity> page = new Page<>(pageNum, pageSize);
        IPage<SeckillActivity> result = seckillActivityService.page(page,
                new LambdaQueryWrapper<SeckillActivity>()
                        .orderByDesc(SeckillActivity::getCreateTime));

        return result.convert(this::convertToVO);
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

        SeckillActivity activity = seckillActivityService.getById(id);
        if (activity == null) {
            throw new BizException(BizErrorCode.SECKILL_ACTIVITY_NOT_EXIST);
        }

        return convertToVO(activity);
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

        SeckillActivity activity = seckillActivityService.getById(id);
        if (activity == null) {
            throw new BizException(BizErrorCode.SECKILL_ACTIVITY_NOT_EXIST);
        }

        BeanUtils.copyProperties(dto, activity, "id", "createTime");
        activity.setUpdateTime(LocalDateTime.now());

        seckillActivityService.updateById(activity);

        return convertToVO(activity);
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

        SeckillActivity activity = seckillActivityService.getById(id);
        if (activity == null) {
            throw new BizException(BizErrorCode.SECKILL_ACTIVITY_NOT_EXIST);
        }

        // 清除缓存
        seckillManager.clearSeckillCache(id);

        return seckillActivityService.removeById(id);
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

        SeckillActivity activity = seckillActivityService.getById(id);
        if (activity == null) {
            throw new BizException(BizErrorCode.SECKILL_ACTIVITY_NOT_EXIST);
        }

        // 初始化Redis库存（如果尚未初始化）
        seckillManager.initializeStock(id);
        
        return true;
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

        // 查询所有秒杀活动相关的审核记录
        AuditParamsDTO queryDTO = new AuditParamsDTO();
        queryDTO.setPage(pageNum);
        queryDTO.setPageSize(pageSize);
        
        return auditService.pageQuery(queryDTO);
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

        // 通过审核
        AuditDecisionDTO decision = AuditDecisionDTO.builder()
                .auditId(id)
                .approved(true)
                .reason("审核通过")
                .build();
        
        auditAppService.handleAuditDecision(decision, "SECKILL_ACTIVITY");
        return true;
    }

    /**
     * 驳回申请
     * POST /admin/seckill/applies/:id/reject
     *
     * @param id     申请ID（审核ID）
     * @param reasonMap 驳回原因
     * @return 是否驳回成功
     */
    @PostMapping("/applies/{id}/reject")
    public boolean rejectApply(@PathVariable Long id, @RequestBody java.util.Map<String, String> reasonMap) {
        log.info("驳回秒杀申请，申请ID: {}", id);

        String reason = reasonMap.get("reason");
        
        // 驳回审核
        AuditDecisionDTO decision = AuditDecisionDTO.builder()
                .auditId(id)
                .approved(false)
                .reason(reason != null ? reason : "申请被驳回")
                .build();
        
        auditAppService.handleAuditDecision(decision, "SECKILL_ACTIVITY");
        return true;
    }

    // ==================== 内部工具方法 ====================

    /**
     * 将Entity转换为VO
     */
    private SeckillActivityVO convertToVO(SeckillActivity activity) {
        SeckillActivityVO vo = new SeckillActivityVO();
        BeanUtils.copyProperties(activity, vo);

        // 设置活动状态
        Integer status = seckillManager.checkSeckillStatus(activity.getId());
        vo.setStatus(status);

        // 获取剩余库存
        Long remainingStock = seckillManager.getRemainingStock(activity.getId());
        vo.setRemainingStock(remainingStock.intValue());

        return vo;
    }
}
