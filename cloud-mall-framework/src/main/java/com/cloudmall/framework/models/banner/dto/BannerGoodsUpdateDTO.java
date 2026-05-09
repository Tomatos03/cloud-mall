package com.cloudmall.framework.models.banner.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 轮播图商品关联更新 DTO
 *
 * @author Tomatos
 * @date 2025/12/17
 */
@Data
public class BannerGoodsUpdateDTO implements Serializable {
    /**
     * 轮播图ID
     */
    private Long bannerId;

    /**
     * 商品ID
     */
    private Long goodsId;
}
