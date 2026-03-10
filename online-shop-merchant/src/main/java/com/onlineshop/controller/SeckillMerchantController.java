package com.onlineshop.controller;

import java.util.List;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.onlineshop.framework.models.audit.application.impl.SeckillGoodsAuditor;
import com.onlineshop.framework.models.audit.dto.AuditSubmitDTO;
import com.onlineshop.framework.models.audit.dto.SeckillGoodsAuditItemDTO;
import com.onlineshop.framework.models.audit.enums.AuditBizType;
import com.onlineshop.framework.models.seckill.application.SeckillAppService;
import com.onlineshop.framework.models.seckill.dto.SeckillActivityParamsDTO;
import com.onlineshop.framework.models.seckill.dto.SeckillGoodsDTO;
import com.onlineshop.framework.models.seckill.dto.SeckillGoodsParamsDTO;
import com.onlineshop.framework.models.seckill.service.SeckillActivityService;
import com.onlineshop.framework.models.seckill.vo.SeckillActivityVO;
import com.onlineshop.framework.utils.AuthUserUtils;

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
    private final SeckillAppService seckillAppService;
    private final SeckillActivityService seckillActivityService;
    private final SeckillGoodsAuditor seckillGoodsAuditor;

    /**
     * 获取可报名活动列表
     * GET /merchant/seckill/activities/list
     *
     * @param params 查询参数
     * @return 报名中的活动列表
     */
    @GetMapping("/activities")
    public IPage<SeckillActivityVO> getAvailableActivities(SeckillActivityParamsDTO params) {
        log.debug("商家查询可报名活动列表，页码: {}, 每页数量: {}", params.getPage(), params.getPageSize());
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
        log.debug("商家查询活动详情，活动ID: {}", id);
        return seckillActivityService.getSeckillActivityVO(id);
    }

    /**
     * 申请商品参与秒杀活动
     * POST /merchant/seckill/activities/{id}/goods
     *
     * @param id       活动ID
     * @param items    批量申请参数
     */
    @PostMapping("/activities/{id}/goods")
    public void applyActivityGoods(@PathVariable Long id, @Valid @RequestBody List<SeckillGoodsAuditItemDTO> items) {
        log.info("商家批量申请商品参与秒杀，活动ID: {}, 商品数量: {}", id, CollUtil.size(items));
        AuditSubmitDTO<SeckillGoodsAuditItemDTO> submitDTO = AuditSubmitDTO.of(
                AuditBizType.SECKILL_GOODS.getCode(),
                id,
                items
        );
        seckillGoodsAuditor.submitAudit(submitDTO);
    }

    /**
     * 获取活动中的商品（仅当前商家）
     * GET /merchant/seckill/activities/{id}/goods
     *
     * @param id     活动ID
     * @param params 秒杀商品分页参数
     * @return 当前商家在该活动下的商品分页列表
     */
    @GetMapping("/activities/{id}/goods")
    public IPage<SeckillGoodsDTO> getActivityGoods(@PathVariable Long id, SeckillGoodsParamsDTO params) {
        log.debug("商家查询活动商品，活动ID: {}, 页码: {}, 每页数量: {}", id, params.getPage(), params.getPageSize());

        params.setActivityId(id);
        params.setMerchantId(AuthUserUtils.getUserId());
        return seckillAppService.pageSeckillActivityGoods(params);
    }
}
