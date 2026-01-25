package com.onlineshop.controller;

import com.onlineshop.framework.models.banner.IBannerService;
import com.onlineshop.framework.models.banner.vo.HomeBannerVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *
 *
 * @author : Tomatos
 * @date : 2025/12/18
 */
@RestController
@RequestMapping("/web/banner")
public class BannerWebController {
    @Autowired
    private IBannerService bannerService;

    @GetMapping
    public List<HomeBannerVO> getRecommendBanner() {
        return bannerService.getRecommendBanner();
    }
}