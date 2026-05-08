package com.onlineshop.framework.models.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineshop.framework.models.coupon.entity.CouponTemplate;
import com.onlineshop.framework.models.coupon.entity.UserCoupon;
import com.onlineshop.framework.models.coupon.enums.CouponUserStatus;
import com.onlineshop.framework.models.coupon.mapper.UserCouponMapper;
import com.onlineshop.framework.models.coupon.service.ICouponTemplateService;
import com.onlineshop.framework.models.coupon.service.IUserCouponService;
import com.onlineshop.framework.models.coupon.vo.UserCouponVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class UserCouponServiceImpl extends ServiceImpl<UserCouponMapper, UserCoupon> implements IUserCouponService {

    @Autowired
    private ICouponTemplateService couponTemplateService;

    @Override
    public UserCoupon getByUserAndTemplate(Long userId, Long templateId) {
        return lambdaQuery()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getTemplateId, templateId)
                .eq(UserCoupon::getStatus, CouponUserStatus.UNUSED.getCode())
                .one();
    }

    @Override
    public List<UserCoupon> listByUserAndTemplate(Long userId, Long templateId) {
        return lambdaQuery()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getTemplateId, templateId)
                .list();
    }

    @Override
    public IPage<UserCouponVO> pageQueryUserCoupons(Long userId, Integer status, Integer page, Integer pageSize) {
        Page<UserCoupon> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .eq(status != null, UserCoupon::getStatus, status)
                .orderByDesc(UserCoupon::getCreateTime);
        return this.page(pageParam, wrapper).convert(this::convertToVO);
    }

    @Override
    public boolean lockCoupon(Long userCouponId, String orderNo) {
        UserCoupon userCoupon = getById(userCouponId);
        CouponUserStatus.of(userCoupon.getStatus()).validateTransferTo(CouponUserStatus.LOCKED);

        userCoupon.setStatus(CouponUserStatus.LOCKED.getCode());
        userCoupon.setOrderNo(orderNo);
        return updateById(userCoupon);
    }

    @Override
    public boolean useCoupon(String orderNo) {
        UserCoupon userCoupon = lambdaQuery()
                .eq(UserCoupon::getOrderNo, orderNo)
                .eq(UserCoupon::getStatus, CouponUserStatus.LOCKED.getCode())
                .one();
        if (userCoupon == null) {
            return false;
        }

        CouponUserStatus.of(userCoupon.getStatus()).validateTransferTo(CouponUserStatus.USED);
        userCoupon.setStatus(CouponUserStatus.USED.getCode());
        userCoupon.setUsedTime(java.time.LocalDateTime.now());
        return updateById(userCoupon);
    }

    @Override
    public boolean releaseCoupon(String orderNo) {
        UserCoupon userCoupon = lambdaQuery()
                .eq(UserCoupon::getOrderNo, orderNo)
                .eq(UserCoupon::getStatus, CouponUserStatus.LOCKED.getCode())
                .one();
        if (userCoupon == null) {
            return false;
        }

        return releaseCouponById(userCoupon.getId());
    }

    @Override
    public boolean releaseCouponById(Long userCouponId) {
        UserCoupon userCoupon = getById(userCouponId);
        if (userCoupon == null) {
            return false;
        }

        CouponUserStatus.of(userCoupon.getStatus()).validateTransferTo(CouponUserStatus.UNUSED);
        userCoupon.setStatus(CouponUserStatus.UNUSED.getCode());
        userCoupon.setOrderNo(null);
        return updateById(userCoupon);
    }

    private UserCouponVO convertToVO(UserCoupon userCoupon) {
        UserCouponVO vo = new UserCouponVO();
        vo.setId(userCoupon.getId());
        vo.setTemplateId(userCoupon.getTemplateId());
        vo.setStatus(userCoupon.getStatus());
        vo.setExpireTime(userCoupon.getExpireTime());
        vo.setUsedTime(userCoupon.getUsedTime());

        CouponTemplate template = couponTemplateService.getById(userCoupon.getTemplateId());
        if (template != null) {
            vo.setCouponName(template.getName());
            vo.setCouponType(template.getType());
            vo.setDiscountAmount(template.getDiscountAmount());
            vo.setDiscountRate(template.getDiscountRate());
            vo.setMaxDiscountAmount(template.getMaxDiscountAmount());
            vo.setMinOrderAmount(template.getMinOrderAmount());
        }
        return vo;
    }
}
