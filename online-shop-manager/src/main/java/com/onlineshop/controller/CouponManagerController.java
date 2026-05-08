package com.onlineshop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlineshop.framework.models.coupon.dto.CouponTemplateDTO;
import com.onlineshop.framework.models.coupon.dto.CouponTemplateParamsDTO;
import com.onlineshop.framework.models.coupon.service.ICouponTemplateService;
import com.onlineshop.framework.models.coupon.vo.CouponTemplateVO;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/manager/coupon")
@PreAuthorize("hasAuthority('coupon:view')")
public class CouponManagerController {

    @Autowired
    private ICouponTemplateService couponTemplateService;

    @PostMapping("/template")
    @PreAuthorize("hasAuthority('coupon:add')")
    public CouponTemplateVO createTemplate(@RequestBody CouponTemplateDTO dto) {
        return couponTemplateService.createTemplate(dto);
    }

    @PutMapping("/template/{id}")
    @PreAuthorize("hasAuthority('coupon:edit')")
    public CouponTemplateVO updateTemplate(@PathVariable @NotNull Long id, @RequestBody CouponTemplateDTO dto) {
        return couponTemplateService.updateTemplate(id, dto);
    }

    @GetMapping("/template/{id}")
    public CouponTemplateVO getTemplate(@PathVariable @NotNull Long id) {
        return couponTemplateService.getTemplateVO(id);
    }

    @GetMapping("/template/list")
    public IPage<CouponTemplateVO> listTemplates(CouponTemplateParamsDTO params) {
        return couponTemplateService.pageQueryTemplates(params);
    }

    @PostMapping("/template/{id}/activate")
    @PreAuthorize("hasAuthority('coupon:edit')")
    public boolean activateTemplate(@PathVariable @NotNull Long id) {
        return couponTemplateService.activateTemplate(id);
    }

    @PostMapping("/template/{id}/pause")
    @PreAuthorize("hasAuthority('coupon:edit')")
    public boolean pauseTemplate(@PathVariable @NotNull Long id) {
        return couponTemplateService.pauseTemplate(id);
    }

    @DeleteMapping("/template/{id}")
    @PreAuthorize("hasAuthority('coupon:delete')")
    public boolean deleteTemplate(@PathVariable @NotNull Long id) {
        return couponTemplateService.removeById(id);
    }
}
