package com.cloudmall.framework.models.coupon.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudmall.framework.models.coupon.entity.CouponTemplateScope;
import com.cloudmall.framework.models.coupon.mapper.CouponTemplateScopeMapper;
import com.cloudmall.framework.models.coupon.service.ICouponTemplateScopeService;
import org.springframework.stereotype.Service;

@Service
public class CouponTemplateScopeServiceImpl extends ServiceImpl<CouponTemplateScopeMapper, CouponTemplateScope> implements ICouponTemplateScopeService {
}
