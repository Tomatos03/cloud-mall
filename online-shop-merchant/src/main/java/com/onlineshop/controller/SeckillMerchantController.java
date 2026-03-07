package com.onlineshop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlineshop.framework.models.audit.application.IAuditAppService;
import com.onlineshop.framework.models.audit.dto.AuditParamsDTO;
import com.onlineshop.framework.models.audit.service.IAuditService;
import com.onlineshop.framework.models.seckill.dto.SeckillActivityParamsDTO;
import com.onlineshop.framework.models.seckill.dto.SeckillGoodsDTO;
import com.onlineshop.framework.models.seckill.dto.SeckillGoodsParamsDTO;
import com.onlineshop.framework.models.seckill.entity.SeckillActivity;
import com.onlineshop.framework.models.seckill.service.SeckillActivityService;
import com.onlineshop.framework.models.seckill.service.SeckillGoodsService;
import com.onlineshop.framework.models.seckill.vo.SeckillActivityVO;
import com.onlineshop.framework.utils.AuthUserUtils;
import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.exception.BizException;
import com.onlineshop.framework.utils.AssertUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 商家秒杀活动控制器
 * 处理商家端秒杀活动申请和查询
 *
 * @author Tomatos
 * @date 2026/2/26
 */
@Slf4j
@RestController
@RequestMapping("/seckill")
@RequiredArgsConstructor
public class SeckillMerchantController {
    private final SeckillActivityService seckillActivityService;
    private final SeckillGoodsService seckillGoodsService;
    private final IAuditAppService auditAppService;
    private final IAuditService auditService;

    /**
     * 获取可报名活动列表
     * GET /merchant/seckill/activities/list
     *
     * @param params 查询参数
     * @return 报名中的活动列表
     */
    @GetMapping("/activities")
    public IPage<SeckillActivityVO> getAvailableActivities(SeckillActivityParamsDTO params) {
        log.info("商家查询可报名活动列表，页码: {}, 每页数量: {}", params.getPage(), params.getPageSize());
        return seckillActivityService.listActivities(params);
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
        return seckillActivityService.getSeckillActivityVO(id);
    }

//    /**
//     * 获取活动中的商品（审核通过和待审核）
//     * GET /merchant/seckill/activities/:id/goods
//     *
//     * @param id 活动ID
//     * @param params 查询参数（包含分页信息）
//     * @return 商品分页列表
//     */
//    @GetMapping("/activities/{id}/goods")
//    public IPage<SeckillGoodsItem> getActivityGoods(@PathVariable Long id,
//                                                     SeckillActivityGoodsParamsDTO params) {
//        log.info("商家查询活动中的商品，活动ID: {}", id);
//        params.setActivityId(id);
//        return auditAppService.getSeckillActivityGoods(params);
//    }

//    /**
//     * 提交申请加入活动
//     * POST /merchant/seckill/applies
//     *
//     * 统一采用批量模型，即使只提交1个商品也需要放在items数组中
//     *
//     * 请求示例（单商品）：
//     * {
//     *   "activityId": 100,
//     *   "items": [
//     *     {"productId": 1001, "seckillPrice": 99.99, "stock": 100}
//     *   ]
//     * }
//     *
//     * 请求示例（多商品）：
//     * {
//     *   "activityId": 100,
//     *   "items": [
//     *     {"productId": 1001, "seckillPrice": 99.99, "stock": 100},
//     *     {"productId": 1002, "seckillPrice": 49.99, "stock": 200}
//     *   ]
//     * }
//     *
//     * @param request 秒杀活动审核请求
//     * @return 无返回值，异步处理
//     */
//    @PostMapping("/applies")
//    public void submitApply(@RequestBody SeckillGoodsAuditRequest request) {
//        log.info("商家提交秒杀活动申请，活动ID: {}, 商品数量: {}",
//                request.getActivityId(), request.getItems().size());
//
//        // 设置申请人信息
//        Long merchantId = AuthUserUtils.getUserId();
//        request.setApplicantId(merchantId);
//        request.setApplicantName(AuthUserUtils.getUsername());
//        request.setType("SECKILL_ACTIVITY");
//
//        auditAppService.submitAudit(request);
//        log.info("秒杀活动申请已提交");
//    }

    /**
     * 获取我的申请列表
     * GET /merchant/seckill/applies/list
     *
     * @param params 查询参数
     * @return 申请列表
     */
    @GetMapping("/applies/list")
    public IPage<?> getMyApplies(SeckillActivityParamsDTO params) {
        log.info("商家查询自己的申请列表，页码: {}, 每页数量: {}", params.getPage(), params.getPageSize());

        Long merchantId = AuthUserUtils.getUserId();
        
        // 通过审核服务查询该商家的所有申请
        AuditParamsDTO queryDTO = new AuditParamsDTO();
        queryDTO.setPage(params.getPage());
        queryDTO.setPageSize(params.getPageSize());
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
//
//    /**
//     * 修改申请（仅待审核/已驳回状态可修改）
//     * PUT /merchant/seckill/applies/:id
//     *
//     * @param id      申请ID
//     * @param request 更新的申请信息
//     * @return 无返回值，异步处理
//     */
//    @PutMapping("/applies/{id}")
//    public void updateApply(@PathVariable Long id,
//                           @RequestBody SeckillGoodsAuditRequest request) {
//        log.info("商家修改申请，申请ID: {}", id);
//
//        Long merchantId = AuthUserUtils.getUserId();
//
//        // 验证权限 - 申请必须属于当前商家
//        Object auditRecord = auditService.getAuditById(id);
//
//        // 设置申请人信息
//        request.setApplicantId(merchantId);
//        request.setApplicantName(AuthUserUtils.getUsername());
//        request.setTargetId(id);
//        request.setType("SECKILL_ACTIVITY");
//
//        // 重新提交审核
//        auditAppService.submitAudit(request);
//    }

    /**
     * 获取活动中我的商品
     * GET /merchant/seckill/my-products/list
     *
     * @param params 秒杀商品查询参数
     * @return 商品列表
     */
    @GetMapping("/my-products/list")
    public IPage<SeckillGoodsDTO> getMyProductsInActivity(SeckillGoodsParamsDTO params) {
        log.info("商家查询活动中的商品，活动ID: {}", params.getActivityId());

        Long merchantId = AuthUserUtils.getUserId();

        // 验证活动存在
        SeckillActivity activity = seckillActivityService.getById(params.getActivityId());
        if (activity == null) {
            throw new BizException(BizErrorCode.SECKILL_ACTIVITY_NOT_EXIST);
        }

        // 通过service获取商家的秒杀商品
        return seckillGoodsService.getMyProductsInActivity(params.getActivityId(), merchantId, 
                params.getPage(), params.getPageSize());
    }

    /**
     * 获取活动中已审核通过的商品（仅限本店铺）
     * GET /merchant/seckill/approved-products/list
     *
     * @param params 秒杀商品查询参数
     * @return 本店铺已审核通过的商品列表
     */
    @GetMapping("/approved-products/list")
    public IPage<SeckillGoodsDTO> getMyApprovedProducts(SeckillGoodsParamsDTO params) {
        log.info("商家查询活动中已审核通过的商品，活动ID: {}, 页码: {}, 每页数量: {}", 
                params.getActivityId(), params.getPage(), params.getPageSize());

        Long merchantId = AuthUserUtils.getUserId();

        // 验证活动存在
        SeckillActivity activity = seckillActivityService.getById(params.getActivityId());
        if (activity == null) {
            throw new BizException(BizErrorCode.SECKILL_ACTIVITY_NOT_EXIST);
        }

        // 获取当前商家在该活动中的已审核通过的秒杀商品
        return seckillGoodsService.getMyProductsInActivity(params.getActivityId(), merchantId,
                params.getPage(), params.getPageSize());
    }
}
