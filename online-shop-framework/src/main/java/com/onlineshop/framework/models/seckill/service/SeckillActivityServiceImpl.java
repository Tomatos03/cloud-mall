package com.onlineshop.framework.models.seckill.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.models.audit.application.IAuditAppService;
import com.onlineshop.framework.models.audit.dto.AuditDecisionDTO;
import com.onlineshop.framework.models.audit.dto.AuditParamsDTO;
import com.onlineshop.framework.models.audit.service.IAuditService;
import com.onlineshop.framework.models.seckill.dto.SeckillActivityDTO;
import com.onlineshop.framework.models.seckill.entity.SeckillActivity;
import com.onlineshop.framework.models.seckill.manager.SeckillManager;
import com.onlineshop.framework.models.seckill.mapper.SeckillActivityMapper;
import com.onlineshop.framework.models.seckill.vo.SeckillActivityVO;
import com.onlineshop.framework.utils.AssertUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 秒杀活动服务实现
 * 
 * 实现了秒杀活动的各类业务操作，包括：
 * - 活动CRUD
 * - 活动启动和状态管理
 * - 审核申请管理
 * - VO转换和数据组合
 */
@Slf4j
@Service
public class SeckillActivityServiceImpl extends ServiceImpl<SeckillActivityMapper, SeckillActivity> implements SeckillActivityService {

    @Autowired
    private SeckillManager seckillManager;

    @Autowired
    private IAuditAppService auditAppService;

    @Autowired
    private IAuditService auditService;

    // ==================== 活动查询接口实现 ====================

    @Override
    public SeckillActivityVO getSeckillActivityVO(Long id) {
        log.info("查询秒杀活动详情，ID: {}", id);

        SeckillActivity activity = getById(id);
        AssertUtils.notNull(activity, BizErrorCode.SECKILL_ACTIVITY_NOT_EXIST);

        return convertToVO(activity);
    }

    @Override
    public IPage<SeckillActivityVO> listActivities(Integer pageNum, Integer pageSize) {
        log.info("查询秒杀活动列表，页码: {}, 每页数量: {}", pageNum, pageSize);

        Page<SeckillActivity> page = new Page<>(pageNum, pageSize);
        IPage<SeckillActivity> result = this.page(page,
                new LambdaQueryWrapper<SeckillActivity>()
                        .orderByDesc(SeckillActivity::getCreateTime));

        return result.convert(this::convertToVO);
    }

    // ==================== 活动管理接口实现 ====================

    @Override
    public SeckillActivityVO createActivity(SeckillActivityDTO dto) {
        log.info("创建秒杀活动，产品ID: {}", dto.getProductId());

        SeckillActivity activity = new SeckillActivity();
        BeanUtils.copyProperties(dto, activity);
        activity.setCreateTime(LocalDateTime.now());
        activity.setUpdateTime(LocalDateTime.now());

        save(activity);

        // 初始化Redis库存
        seckillManager.initializeStock(activity.getId());

        return convertToVO(activity);
    }

    @Override
    public SeckillActivityVO updateActivity(Long id, SeckillActivityDTO dto) {
        log.info("更新秒杀活动，ID: {}", id);

        SeckillActivity activity = getById(id);
        AssertUtils.notNull(activity, BizErrorCode.SECKILL_ACTIVITY_NOT_EXIST);

        BeanUtils.copyProperties(dto, activity, "id", "createTime");
        activity.setUpdateTime(LocalDateTime.now());

        updateById(activity);

        return convertToVO(activity);
    }

    @Override
    public boolean deleteActivity(Long id) {
        log.info("删除秒杀活动，ID: {}", id);

        SeckillActivity activity = getById(id);
        AssertUtils.notNull(activity, BizErrorCode.SECKILL_ACTIVITY_NOT_EXIST);

        // 清除缓存
        seckillManager.clearSeckillCache(id);

        return removeById(id);
    }

    @Override
    public boolean startActivity(Long id) {
        log.info("开始秒杀活动，ID: {}", id);

        SeckillActivity activity = getById(id);
        AssertUtils.notNull(activity, BizErrorCode.SECKILL_ACTIVITY_NOT_EXIST);

        // 初始化Redis库存（如果尚未初始化）
        seckillManager.initializeStock(id);

        return true;
    }

    // ==================== 审核管理接口实现 ====================

    @Override
    public IPage<?> listAuditApplies(Integer pageNum, Integer pageSize) {
        log.info("查询秒杀申请列表，页码: {}, 每页数量: {}", pageNum, pageSize);

        // 查询所有秒杀活动相关的审核记录
        AuditParamsDTO queryDTO = new AuditParamsDTO();
        queryDTO.setPage(pageNum);
        queryDTO.setPageSize(pageSize);

        return auditService.pageQuery(queryDTO);
    }

    @Override
    public boolean approveApply(Long auditId) {
        log.info("通过秒杀申请，申请ID: {}", auditId);

        // 通过审核
        AuditDecisionDTO decision = AuditDecisionDTO.builder()
                .auditId(auditId)
                .approved(true)
                .reason("审核通过")
                .build();

        auditAppService.handleAuditDecision(decision, "SECKILL_ACTIVITY");
        return true;
    }

    @Override
    public boolean rejectApply(Long auditId, String reason) {
        log.info("驳回秒杀申请，申请ID: {}", auditId);

        // 驳回审核
        AuditDecisionDTO decision = AuditDecisionDTO.builder()
                .auditId(auditId)
                .approved(false)
                .reason(reason != null ? reason : "申请被驳回")
                .build();

        auditAppService.handleAuditDecision(decision, "SECKILL_ACTIVITY");
        return true;
    }

    // ==================== 内部工具方法 ====================

    /**
     * 将Entity转换为VO，包含实时库存和状态
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