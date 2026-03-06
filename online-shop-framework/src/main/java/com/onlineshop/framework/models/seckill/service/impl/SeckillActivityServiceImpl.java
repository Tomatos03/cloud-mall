package com.onlineshop.framework.models.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.models.audit.dto.AuditParamsDTO;
import com.onlineshop.framework.models.audit.service.IAuditService;
import com.onlineshop.framework.models.seckill.dto.SeckillActivityDTO;
import com.onlineshop.framework.models.seckill.dto.SeckillActivityParamsDTO;
import com.onlineshop.framework.models.seckill.dto.SeckillGoodsParamsDTO;
import com.onlineshop.framework.models.seckill.enums.SeckillActivityStatus;
import com.onlineshop.framework.models.seckill.entity.SeckillActivity;
import com.onlineshop.framework.models.seckill.mapper.SeckillActivityMapper;
import com.onlineshop.framework.models.seckill.service.SeckillActivityService;
import com.onlineshop.framework.models.seckill.service.SeckillGoodsService;
import com.onlineshop.framework.models.seckill.vo.SeckillActivityVO;
import com.onlineshop.framework.utils.AssertUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 秒杀活动服务实现
 * 
 * 实现秒杀活动的CRUD操作：
 * - 创建、查询、更新、删除秒杀活动
 * - 启动秒杀活动
 * - 查询审核申请列表
 * 
 * 注意：
 * - createTime 和 updateTime 由 MyMetaObjectHandler 自动填充
 * - createUser 和 updateUser 由 MyMetaObjectHandler 自动填充
 */
@Slf4j
@Service
public class SeckillActivityServiceImpl extends ServiceImpl<SeckillActivityMapper, SeckillActivity> implements SeckillActivityService {
    @Autowired
    private IAuditService auditService;
    @Autowired
    private SeckillGoodsService seckillGoodsService;

    @Override
    public SeckillActivityVO getSeckillActivityVO(Long id) {
        log.info("查询秒杀活动详情，ID: {}", id);

        SeckillActivity activity = getById(id);
        AssertUtils.notNull(activity, BizErrorCode.SECKILL_ACTIVITY_NOT_EXIST);

        return convertToVO(activity);
    }

    @Override
    public IPage<SeckillActivityVO> listActivities(SeckillActivityParamsDTO params) {
        log.info("查询秒杀活动列表，页码: {}, 每页数量: {}", params.getPage(), params.getPageSize());

        Page<SeckillActivity> page = new Page<>(params.getPage(), params.getPageSize());
        IPage<SeckillActivity> result = this.page(page,
                new LambdaQueryWrapper<SeckillActivity>()
                        .orderByDesc(SeckillActivity::getCreateTime));

        return result.convert(this::convertToVO);
    }

    // ==================== Mutation Methods ====================

    @Override
    public SeckillActivityVO createActivity(SeckillActivityDTO dto) {
        log.info("创建秒杀活动，名称: {}", dto.getName());

        SeckillActivity activity = new SeckillActivity();
        BeanUtils.copyProperties(dto, activity);
        activity.setStatus(SeckillActivityStatus.REGISTRATION.getCode()); // 默认状态为"报名中"
        save(activity);

        return convertToVO(activity);
    }

    @Override
    public SeckillActivityVO updateActivity(Long id, SeckillActivityDTO dto) {
        log.info("更新秒杀活动，ID: {}", id);

        SeckillActivity activity = getById(id);
        AssertUtils.notNull(activity, BizErrorCode.SECKILL_ACTIVITY_NOT_EXIST);

        BeanUtils.copyProperties(dto, activity, "id", "createTime", "updateTime");
        // 更新时间由 MyMetaObjectHandler 自动填充

        updateById(activity);

        return convertToVO(activity);
    }

    @Override
    public boolean deleteActivity(Long id) {
        log.info("删除秒杀活动，ID: {}", id);

        SeckillActivity activity = getById(id);
        AssertUtils.notNull(activity, BizErrorCode.SECKILL_ACTIVITY_NOT_EXIST);

        return removeById(id);
    }

    @Override
    public boolean startActivity(Long id) {
        log.info("启动秒杀活动，ID: {}", id);

        SeckillActivity activity = getById(id);
        AssertUtils.notNull(activity, BizErrorCode.SECKILL_ACTIVITY_NOT_EXIST);

        // 更新活动状态为"进行中"
        activity.setStatus(SeckillActivityStatus.IN_PROGRESS.getCode());
        updateById(activity);

        return true;
    }

    // ==================== Audit Methods ====================

    @Override
    public IPage<?> listAuditApplies(SeckillActivityParamsDTO params) {
        log.info("查询秒杀申请列表，页码: {}, 每页数量: {}", params.getPage(), params.getPageSize());

        AuditParamsDTO queryDTO = new AuditParamsDTO();
        queryDTO.setPage(params.getPage());
        queryDTO.setPageSize(params.getPageSize());

        return auditService.pageQuery(queryDTO);
    }

    @Override
    public IPage<?> getApprovedGoodsInActivity(SeckillGoodsParamsDTO params) {
        log.info("查询活动中已审核通过的商品，活动ID: {}, 页码: {}, 每页数量: {}", 
                params.getActivityId(), params.getPage(), params.getPageSize());

        // 验证活动存在
        SeckillActivity activity = getById(params.getActivityId());
        AssertUtils.notNull(activity, BizErrorCode.SECKILL_ACTIVITY_NOT_EXIST);

        // 获取活动中所有已审核通过的秒杀商品
        return seckillGoodsService.getActivityProducts(params);
    }

    // ==================== Helper Methods ====================

    /**
     * 将Entity转换为VO
     */
    private SeckillActivityVO convertToVO(SeckillActivity activity) {
        SeckillActivityVO vo = new SeckillActivityVO();
        BeanUtils.copyProperties(activity, vo);
        return vo;
    }
}