package com.cloudmall.framework.models.banner.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 *
 * @author : Tomatos
 * @date : 2025/12/28
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HomeBannerVO {
    private String imageUrl;
    private Long goodsId;
}