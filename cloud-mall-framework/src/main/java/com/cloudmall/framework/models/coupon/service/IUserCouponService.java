package com.cloudmall.framework.models.coupon.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudmall.framework.models.coupon.entity.UserCoupon;
import com.cloudmall.framework.models.coupon.vo.UserCouponVO;

import java.util.List;

public interface IUserCouponService extends IService<UserCoupon> {

    UserCoupon getByUserAndTemplate(Long userId, Long templateId);

    List<UserCoupon> listByUserAndTemplate(Long userId, Long templateId);

    IPage<UserCouponVO> pageQueryUserCoupons(Long userId, Integer status, Integer page, Integer pageSize);

    boolean lockCoupon(Long userCouponId, String orderNo);

    boolean useCoupon(String orderNo);

    boolean releaseCoupon(String orderNo);

    boolean releaseCouponById(Long userCouponId);
}
