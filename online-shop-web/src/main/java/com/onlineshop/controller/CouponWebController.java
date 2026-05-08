package com.onlineshop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlineshop.framework.models.coupon.application.ICouponAppService;
import com.onlineshop.framework.models.coupon.application.vo.CouponPoolVO;
import com.onlineshop.framework.models.coupon.dto.CouponClaimDTO;
import com.onlineshop.framework.models.coupon.service.IUserCouponService;
import com.onlineshop.framework.models.coupon.vo.UserCouponVO;
import com.onlineshop.framework.utils.AuthUserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/web/coupon")
public class CouponWebController {

    @Autowired
    private ICouponAppService couponAppService;

    @Autowired
    private IUserCouponService userCouponService;

    @GetMapping("/pool")
    public List<CouponPoolVO> listCouponPool() {
        return couponAppService.listCouponPool();
    }

    @PostMapping("/claim")
    public boolean claimCoupon(@RequestBody CouponClaimDTO dto) {
        return couponAppService.claimCoupon(dto.getTemplateId());
    }

    @GetMapping("/my")
    public IPage<UserCouponVO> myCoupons(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = AuthUserUtils.getUserId();
        return userCouponService.pageQueryUserCoupons(userId, status, page, pageSize);
    }
}
