package com.cloudmall.framework.models.goods.application.vo;

import com.cloudmall.framework.models.goods.sku.SkuDTO;
import com.cloudmall.framework.models.goods.spec.dto.SpecificationsDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * SKU详情VO
 *
 * @author Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsDetailVO {
    /**
     * 商品详情图片
     */
    private List<String> descriptionImageUrls;

    /**
     * 规格列表
     */
    @NotEmpty(message = "规格不能为空")
    @Valid
    private List<SpecificationsDTO> specifications;

    /**
     * SKU列表
     */
    @NotEmpty(message = "SKU不能为空")
    @Valid
    private List<SkuDTO> skus;
}