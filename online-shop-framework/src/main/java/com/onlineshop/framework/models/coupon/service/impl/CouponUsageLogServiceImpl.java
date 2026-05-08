package com.onlineshop.framework.models.coupon.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineshop.framework.models.coupon.entity.CouponUsageLog;
import com.onlineshop.framework.models.coupon.mapper.CouponUsageLogMapper;
import com.onlineshop.framework.models.coupon.service.ICouponUsageLogService;
import org.springframework.stereotype.Service;

@Service
public class CouponUsageLogServiceImpl extends ServiceImpl<CouponUsageLogMapper, CouponUsageLog> implements ICouponUsageLogService {
}
