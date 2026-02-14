package com.onlineshop.framework.models.banner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 轮播图创建/更新 DTO
 *
 * @author Tomatos
 * @date 2026/1/24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BannerDTO implements Serializable {
    /**
     * 轮播图ID（更新时使用，创建时为空）
     */
    private Long id;

    /**
     * 轮播图图片URL（必填）
     */
    @NotBlank(message = "轮播图URL不能为空")
    private String imageUrl;

    /**
     * 关联商品ID（必填）
     */
    @NotNull(message = "关联商品ID不能为空")
    private Long goodsId;

    /**
     * 是否推荐（可选，默认false）
     */
    @NotNull(message = "推荐状态不能为空")
    private Boolean isRecommend = false;
}
