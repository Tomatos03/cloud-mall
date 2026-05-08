package com.onlineshop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlineshop.framework.models.coupon.dto.CouponTemplateDTO;
import com.onlineshop.framework.models.coupon.dto.CouponTemplateParamsDTO;
import com.onlineshop.framework.models.coupon.service.ICouponTemplateService;
import com.onlineshop.framework.models.coupon.vo.CouponTemplateVO;
import com.onlineshop.framework.utils.AuthUserUtils;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/merchant/coupon")
@RequiredArgsConstructor
public class CouponMerchantController {

    private final ICouponTemplateService couponTemplateService;

    @PostMapping("/template")
    public CouponTemplateVO createTemplate(@RequestBody CouponTemplateDTO dto) {
        dto.setStoreId(AuthUserUtils.getStoreId());
        return couponTemplateService.createTemplate(dto);
    }

    @PutMapping("/template/{id}")
    public CouponTemplateVO updateTemplate(@PathVariable @NotNull Long id, @RequestBody CouponTemplateDTO dto) {
        return couponTemplateService.updateTemplate(id, dto);
    }

    @GetMapping("/template/{id}")
    public CouponTemplateVO getTemplate(@PathVariable @NotNull Long id) {
        return couponTemplateService.getTemplateVO(id);
    }

    @GetMapping("/template/list")
    public IPage<CouponTemplateVO> listTemplates(CouponTemplateParamsDTO params) {
        params.setStoreId(AuthUserUtils.getStoreId());
        return couponTemplateService.pageQueryTemplates(params);
    }

    @PostMapping("/template/{id}/activate")
    public boolean activateTemplate(@PathVariable @NotNull Long id) {
        return couponTemplateService.activateTemplate(id);
    }

    @PostMapping("/template/{id}/pause")
    public boolean pauseTemplate(@PathVariable @NotNull Long id) {
        return couponTemplateService.pauseTemplate(id);
    }
}
