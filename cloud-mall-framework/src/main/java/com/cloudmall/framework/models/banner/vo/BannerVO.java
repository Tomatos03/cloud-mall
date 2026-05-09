package com.cloudmall.framework.models.banner.vo;

import lombok.Data;

/**
 * 轮播图实体类
 *
 * @author Tomatos
 * @date 2025/12/17
 */
@Data
public class BannerVO {
    private Long id;
    private String imageUrl;
    private Long goodsId;
    private String goodsName;
    private Boolean isRecommend;
}