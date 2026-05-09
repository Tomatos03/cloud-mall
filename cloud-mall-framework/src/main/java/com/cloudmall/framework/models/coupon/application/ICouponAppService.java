package com.cloudmall.framework.models.coupon.application;

import com.cloudmall.framework.models.coupon.application.vo.CouponCalcResult;
import com.cloudmall.framework.models.coupon.application.vo.CouponPoolVO;

import java.util.List;
import java.util.Map;

public interface ICouponAppService {

    List<CouponPoolVO> listCouponPool();

    boolean claimCoupon(Long templateId);

    void lockCoupon(Long userCouponId, String orderNo);

    void useCoupon(String orderNo);

    void releaseCoupon(String orderNo);

    Map<Long, CouponCalcResult> calculateDiscount(Map<Long, Long> shopCouponIds, Map<Long, Long> shopTotalPrices, Map<Long, Map<Long, Long>> shopItemPrices);
}
