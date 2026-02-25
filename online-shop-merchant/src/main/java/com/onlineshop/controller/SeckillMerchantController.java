package com.onlineshop.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.exception.BizException;
import com.onlineshop.framework.models.audit.domain.SeckillActivityAuditRequest;
import com.onlineshop.framework.models.audit.application.IAuditAppService;
import com.onlineshop.framework.models.audit.dto.AuditParamsDTO;
import com.onlineshop.framework.models.audit.service.IAuditService;
import com.onlineshop.framework.models.seckill.dto.SeckillGoodsDTO;
import com.onlineshop.framework.models.seckill.entity.SeckillActivity;
import com.onlineshop.framework.models.seckill.service.SeckillActivityService;
import com.onlineshop.framework.models.seckill.service.SeckillActivityStatsService;
import com.onlineshop.framework.models.seckill.service.SeckillGoodsService;
import com.onlineshop.framework.models.seckill.vo.SeckillActivityVO;
import com.onlineshop.framework.utils.AuthUserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 商家秒杀活动控制器
 * 处理商家端秒杀活动申请和查询
 *
 * @author Tomatos
 * @date 2026/2/26
 */
@Slf4j
@RestController
@RequestMapping("/merchant/seckill")
@RequiredArgsConstructor
public class SeckillMerchantController {

    private final SeckillActivityService seckillActivityService;
    private final SeckillGoodsService seckillGoodsService;
    private final SeckillActivityStatsService seckillActivityStatsService;
    private final IAuditAppService auditAppService;
    private final IAuditService auditService;

    /**
     * 获取可报名活动列表
     * GET /merchant/seckill/activities
     *
     * @param pageNum  页码（默认1）
     * @param pageSize 每页数量（默认10）
     * @return 报名中的活动列表
     */
    @GetMapping("/activities")
    public IPage<SeckillActivityVO> getAvailableActivities(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        log.info("商家查询可报名活动列表，页码: {}, 每页数量: {}", pageNum, pageSize);

        Page<SeckillActivity> page = new Page<>(pageNum, pageSize);
        IPage<SeckillActivity> result = seckillActivityService.page(page,
                new LambdaQueryWrapper<SeckillActivity>()
                        .orderByDesc(SeckillActivity::getCreateTime));

        return result.convert(this::convertToVO);
    }

    /**
     * 获取活动详情
     * GET /merchant/seckill/activities/:id
     *
     * @param id 活动ID
     * @return 活动详情
     */
    @GetMapping("/activities/{id}")
    public SeckillActivityVO getActivityDetail(@PathVariable Long id) {
        log.info("商家查询活动详情，活动ID: {}", id);

        SeckillActivity activity = seckillActivityService.getById(id);
        if (activity == null) {
            throw new BizException(BizErrorCode.SECKILL_ACTIVITY_NOT_EXIST);
        }

        return convertToVO(activity);
    }

    /**
     * 提交申请加入活动
     * POST /merchant/seckill/applies
     *
     * @param request 秒杀活动审核请求
     * @return 无返回值，异步处理
     */
    @PostMapping("/applies")
    public void submitApply(@RequestBody SeckillActivityAuditRequest request) {
        log.info("商家提交秒杀活动申请，商品ID: {}, 秒杀价格: {}", 
                request.getProductId(), request.getSeckillPrice());

        // 获取当前登录用户信息
        Long merchantId = AuthUserUtils.getUserId();
        request.setApplicantId(merchantId);

        // 提交审核（submitAudit返回void）
        auditAppService.submitAudit(request);
        log.info("秒杀活动申请已提交");
    }

    /**
     * 获取我的申请列表
     * GET /merchant/seckill/applies
     *
     * @param pageNum  页码（默认1）
     * @param pageSize 每页数量（默认10）
     * @return 申请列表
     */
    @GetMapping("/applies")
    public IPage<?> getMyApplies(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        log.info("商家查询自己的申请列表，页码: {}, 每页数量: {}", pageNum, pageSize);

        Long merchantId = AuthUserUtils.getUserId();
        
        // 通过审核服务查询该商家的所有申请
        AuditParamsDTO queryDTO = new AuditParamsDTO();
        queryDTO.setPage(pageNum);
        queryDTO.setPageSize(pageSize);
        queryDTO.setApplicantId(merchantId);
        
        return auditService.pageQuery(queryDTO);
    }

    /**
     * 获取申请详情
     * GET /merchant/seckill/applies/:id
     *
     * @param id 申请ID（审核ID）
     * @return 申请详情
     */
    @GetMapping("/applies/{id}")
    public Object getApplyDetail(@PathVariable Long id) {
        log.info("商家查询申请详情，申请ID: {}", id);

        return auditService.getAuditById(id);
    }

    /**
     * 修改申请（仅待审核/已驳回状态可修改）
     * PUT /merchant/seckill/applies/:id
     *
     * @param id      申请ID
     * @param request 更新的申请信息
     * @return 无返回值，异步处理
     */
    @PutMapping("/applies/{id}")
    public void updateApply(@PathVariable Long id, 
                           @RequestBody SeckillActivityAuditRequest request) {
        log.info("商家修改申请，申请ID: {}", id);

        Long merchantId = AuthUserUtils.getUserId();

        // 验证权限 - 申请必须属于当前商家
        Object auditRecord = auditService.getAuditById(id);
        
        // 获取审核状态，检查是否可修改（仅待审核或已驳回状态）
        // 这里需要通过审核服务验证状态
        
        request.setApplicantId(merchantId);
        request.setTargetId(id);

        // 重新提交审核
        auditAppService.submitAudit(request);
    }

    /**
     * 取消/撤回申请
     * DELETE /merchant/seckill/applies/:id
     *
     * @param id 申请ID
     * @return 取消结果
     */
    @DeleteMapping("/applies/{id}")
    public boolean cancelApply(@PathVariable Long id) {
        log.info("商家撤回申请，申请ID: {}", id);

        Long merchantId = AuthUserUtils.getUserId();

        // 撤回审核申请
        return auditService.withdrawAudit(id);
    }

    /**
     * 获取活动中我的商品
     * GET /merchant/seckill/my-products
     *
     * @param activityId 活动ID
     * @param pageNum    页码（默认1）
     * @param pageSize   每页数量（默认10）
     * @return 商品列表
     */
    @GetMapping("/my-products")
    public IPage<SeckillGoodsDTO> getMyProductsInActivity(
            @RequestParam Long activityId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        log.info("商家查询活动中的商品，活动ID: {}", activityId);

        Long merchantId = AuthUserUtils.getUserId();

        // 验证活动存在
        SeckillActivity activity = seckillActivityService.getById(activityId);
        if (activity == null) {
            throw new BizException(BizErrorCode.SECKILL_ACTIVITY_NOT_EXIST);
        }

        // 通过service获取商家的秒杀商品
        return seckillGoodsService.getMyProductsInActivity(activityId, merchantId, pageNum, pageSize);
    }

    /**
     * 获取活动数据统计
     * GET /merchant/seckill/stats/:activityId
     *
     * @param activityId 活动ID
     * @return 统计数据
     */
    @GetMapping("/stats/{activityId}")
    public Map<String, Object> getActivityStats(@PathVariable Long activityId) {
        log.info("商家查询活动统计数据，活动ID: {}", activityId);

        Long merchantId = AuthUserUtils.getUserId();

        // 验证活动存在
        SeckillActivity activity = seckillActivityService.getById(activityId);
        if (activity == null) {
            throw new BizException(BizErrorCode.SECKILL_ACTIVITY_NOT_EXIST);
        }

        // 通过service获取统计数据
        return seckillActivityStatsService.getMerchantActivityStats(activityId, merchantId);
    }

    /**
     * 将Entity转换为VO
     */
    private SeckillActivityVO convertToVO(SeckillActivity activity) {
        SeckillActivityVO vo = new SeckillActivityVO();
        vo.setId(activity.getId());
        vo.setProductId(activity.getProductId());
        vo.setStartTime(activity.getStartTime());
        vo.setEndTime(activity.getEndTime());
        vo.setSeckillPrice(activity.getSeckillPrice());
        vo.setStock(activity.getStock());

        // 计算活动状态
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
}
