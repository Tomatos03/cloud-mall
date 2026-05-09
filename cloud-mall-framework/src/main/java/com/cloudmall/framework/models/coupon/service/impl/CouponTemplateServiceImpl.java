package com.cloudmall.framework.models.coupon.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudmall.framework.common.enums.BizErrorCode;
import com.cloudmall.framework.models.coupon.dto.CouponTemplateDTO;
import com.cloudmall.framework.models.coupon.dto.CouponTemplateParamsDTO;
import com.cloudmall.framework.models.coupon.entity.CouponTemplate;
import com.cloudmall.framework.models.coupon.entity.CouponTemplateScope;
import com.cloudmall.framework.models.coupon.enums.CouponScopeType;
import com.cloudmall.framework.models.coupon.enums.CouponStatus;
import com.cloudmall.framework.models.coupon.mapper.CouponTemplateMapper;
import com.cloudmall.framework.models.coupon.service.ICouponTemplateService;
import com.cloudmall.framework.models.coupon.service.ICouponTemplateScopeService;
import com.cloudmall.framework.models.coupon.vo.CouponTemplateVO;
import com.cloudmall.framework.utils.AssertUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class CouponTemplateServiceImpl extends ServiceImpl<CouponTemplateMapper, CouponTemplate> implements ICouponTemplateService {

    private static final String COUPON_STOCK_KEY_PREFIX = "coupon:stock:";

    @Autowired
    private ICouponTemplateScopeService couponTemplateScopeService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CouponTemplateVO createTemplate(CouponTemplateDTO dto) {
        log.info("创建优惠券模板, name: {}", dto.getName());
        CouponTemplate template = new CouponTemplate();
        BeanUtils.copyProperties(dto, template);
        template.setIssuedCount(0);
        template.setStatus(CouponStatus.DRAFT.getCode());
        save(template);

        if (CollUtil.isNotEmpty(dto.getScopeRefIds()) && dto.getScopeType() != CouponScopeType.ALL.getCode()) {
            List<CouponTemplateScope> scopes = dto.getScopeRefIds().stream()
                    .map(refId -> {
                        CouponTemplateScope scope = new CouponTemplateScope();
                        scope.setTemplateId(template.getId());
                        scope.setScopeType(dto.getScopeType());
                        scope.setRefId(refId);
                        return scope;
                    }).toList();
            couponTemplateScopeService.saveBatch(scopes);
        }

        return convertToVO(template);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CouponTemplateVO updateTemplate(Long id, CouponTemplateDTO dto) {
        log.info("更新优惠券模板, id: {}", id);
        CouponTemplate template = getById(id);
        AssertUtils.notNull(template, BizErrorCode.COUPON_TEMPLATE_NOT_ACTIVE);
        AssertUtils.isTrue(template.getStatus() == CouponStatus.DRAFT.getCode(),
                BizErrorCode.COUPON_TEMPLATE_NOT_ACTIVE);

        BeanUtils.copyProperties(dto, template, "id", "createTime", "updateTime", "issuedCount", "status");
        updateById(template);

        if (dto.getScopeType() != CouponScopeType.ALL.getCode() && CollUtil.isNotEmpty(dto.getScopeRefIds())) {
            couponTemplateScopeService.lambdaUpdate()
                    .eq(CouponTemplateScope::getTemplateId, id)
                    .remove();
            List<CouponTemplateScope> scopes = dto.getScopeRefIds().stream()
                    .map(refId -> {
                        CouponTemplateScope scope = new CouponTemplateScope();
                        scope.setTemplateId(id);
                        scope.setScopeType(dto.getScopeType());
                        scope.setRefId(refId);
                        return scope;
                    }).toList();
            couponTemplateScopeService.saveBatch(scopes);
        }

        return convertToVO(template);
    }

    @Override
    public CouponTemplateVO getTemplateVO(Long id) {
        CouponTemplate template = getById(id);
        AssertUtils.notNull(template, BizErrorCode.COUPON_TEMPLATE_NOT_ACTIVE);
        return convertToVO(template);
    }

    @Override
    public IPage<CouponTemplateVO> pageQueryTemplates(CouponTemplateParamsDTO params) {
        Page<CouponTemplate> page = new Page<>(params.getPage(), params.getPageSize());
        LambdaQueryWrapper<CouponTemplate> wrapper = new LambdaQueryWrapper<CouponTemplate>()
                .eq(params.getStoreId() != null, CouponTemplate::getStoreId, params.getStoreId())
                .eq(params.getStatus() != null, CouponTemplate::getStatus, params.getStatus())
                .eq(params.getType() != null, CouponTemplate::getType, params.getType())
                .orderByDesc(CouponTemplate::getCreateTime);
        return this.page(page, wrapper).convert(this::convertToVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean activateTemplate(Long id) {
        log.info("激活优惠券模板, id: {}", id);
        CouponTemplate template = getById(id);
        AssertUtils.notNull(template, BizErrorCode.COUPON_TEMPLATE_NOT_ACTIVE);
        AssertUtils.isTrue(template.getStatus() == CouponStatus.DRAFT.getCode(),
                BizErrorCode.COUPON_TEMPLATE_NOT_ACTIVE);

        template.setStatus(CouponStatus.ACTIVE.getCode());
        updateById(template);

        String stockKey = COUPON_STOCK_KEY_PREFIX + id;
        long ttlSeconds = java.time.Duration.between(java.time.LocalDateTime.now(), template.getEndTime()).getSeconds();
        if (ttlSeconds > 0) {
            stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(template.getTotalCount()), java.time.Duration.ofSeconds(ttlSeconds));
        }
        log.info("优惠券模板激活成功, id: {}, stock: {}", id, template.getTotalCount());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean pauseTemplate(Long id) {
        log.info("暂停优惠券模板, id: {}", id);
        CouponTemplate template = getById(id);
        AssertUtils.notNull(template, BizErrorCode.COUPON_TEMPLATE_NOT_ACTIVE);
        AssertUtils.isTrue(template.getStatus() == CouponStatus.ACTIVE.getCode(),
                BizErrorCode.COUPON_TEMPLATE_NOT_ACTIVE);

        template.setStatus(CouponStatus.PAUSED.getCode());
        return updateById(template);
    }

    private CouponTemplateVO convertToVO(CouponTemplate template) {
        CouponTemplateVO vo = new CouponTemplateVO();
        BeanUtils.copyProperties(template, vo);
        return vo;
    }
}
