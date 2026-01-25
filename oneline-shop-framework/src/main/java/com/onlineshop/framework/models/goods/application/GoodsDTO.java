package com.onlineshop.framework.models.goods.application;

import com.onlineshop.framework.models.goods.sku.SkuDTO;
import com.onlineshop.framework.models.goods.spec.dto.SpecificationsDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 商品发布请求对象（新增/更新）
 *
 * @author Tomatos
 * @date 2026/1/6
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsDTO {
    /**
     * 商品ID（更新时使用）
     */
    private Long goodsId;

    @NotBlank
    private String goodsName;

    /**
     * 分类ID
     */
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    private String categoryIdPath;

    /**
     * 单位ID
     */
    @NotNull(message = "单位ID不能为空")
    private Long unitId;

    /**
     * 单位名称
     */
    private String unitName;

    /**
     * 商品卖点
     */
    @NotBlank
    private String sellPoint;

    @NotEmpty(message = "至少需要一张展示图")
    @Size(min = 1, max = 5, message = "展示图数量应在1-5张之间")
    private List<String> displayImageUrls;

    /**
     * 描述图URL列表（逗号分隔）
     */
    @NotEmpty(message = "至少需要一张描述图")
    @Size(min = 1, max = 8, message = "描述图数量应在1-8张之间")
    private List<String> descriptionImageUrls;

    /**
     * 店铺ID
     */
    @NotNull(message = "店铺ID不能为空")
    private Long storeId;

    /**
     * 店铺名称
     */
    private String storeName;

    /**
     * 状态：1=上架，0=下架
     */
    @NotNull(message = "状态不能为空")
    private Boolean status;

    /**
     * 规格列表
     */
    @Valid
    @NotEmpty(message = "规格不能为空")
    @Size(min = 1, max = 3, message = "规格数量应在1-3之间")
    private List<SpecificationsDTO> specifications;

    /**
     * SKU列表
     */
    @NotEmpty(message = "SKU不能为空")
    @Valid
    private List<SkuDTO> skus;
}