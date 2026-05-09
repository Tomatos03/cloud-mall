package com.cloudmall.framework.models.coupon.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudmall.framework.models.coupon.dto.CouponTemplateDTO;
import com.cloudmall.framework.models.coupon.dto.CouponTemplateParamsDTO;
import com.cloudmall.framework.models.coupon.entity.CouponTemplate;
import com.cloudmall.framework.models.coupon.vo.CouponTemplateVO;

public interface ICouponTemplateService extends IService<CouponTemplate> {

    CouponTemplateVO createTemplate(CouponTemplateDTO dto);

    CouponTemplateVO updateTemplate(Long id, CouponTemplateDTO dto);

    CouponTemplateVO getTemplateVO(Long id);

    IPage<CouponTemplateVO> pageQueryTemplates(CouponTemplateParamsDTO params);

    boolean activateTemplate(Long id);

    boolean pauseTemplate(Long id);
}
