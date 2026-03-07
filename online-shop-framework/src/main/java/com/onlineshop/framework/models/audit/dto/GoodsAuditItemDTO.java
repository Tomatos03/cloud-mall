package com.onlineshop.framework.models.audit.dto;

import com.onlineshop.framework.models.goods.sku.SkuDTO;
import com.onlineshop.framework.models.goods.spec.dto.SpecificationsDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

/**
 * 商品审核项目DTO
 * 代表一个审核批次中的单个商品审核项目
 * 
 * 设计说明：
 * - 对应 AuditItem 表，存储在 snapshot 字段中
 * - 包含提交审核时的所有商品信息
 * - 审核员基于此信息做出批准或拒绝决策
 *
 * @author Tomatos
 * @date 2026/3/7
 */
@Data
public class GoodsAuditItemDTO {
    /**
     * 商品ID (更新商品时使用，新增时为null)
     */
    private Long goodsId;

    /**
     * 商品名称
     */
    @NotBlank(message = "商品名称不能为空")
    private String goodsName;
    
    /**
     * 分类ID
     */
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;
    
    /**
     * 分类ID路径
     */
    private String categoryIdPath;
    
    /**
     * 单位ID
     */
    @NotNull(message = "单位ID不能为空")
    private Long unitId;
    
    /**
     * 单位名称
     */
    @NotBlank(message = "单位名称不能为空")
    private String unitName;
    
    /**
     * 商品卖点
     */
    @NotBlank(message = "商品卖点不能为空")
    private String sellPoint;
    
    /**
     * 展示图URL列表 (1-5张)
     */
    @NotEmpty(message = "展示图不能为空")
    @Size(min = 1, max = 5, message = "展示图数量必须在1-5张之间")
    private List<String> displayImageUrls;
    
    /**
     * 描述图URL列表 (1-8张)
     */
    @NotEmpty(message = "描述图不能为空")
    @Size(min = 1, max = 8, message = "描述图数量必须在1-8张之间")
    private List<String> descriptionImageUrls;
    
    /**
     * 店铺ID
     */
    @NotNull(message = "店铺ID不能为空")
    private Long storeId;
    
    /**
     * 店铺名称
     */
    @NotBlank(message = "店铺名称不能为空")
    private String storeName;
    
    /**
     * 商品状态 (1=上架，0=下架)
     */
    private Boolean status;
    
    /**
     * 规格列表 (1-3个)
     */
    @NotEmpty(message = "规格不能为空")
    @Size(min = 1, max = 3, message = "规格数量必须在1-3个之间")
    @Valid
    private List<SpecificationsDTO> specifications;
    
    /**
     * SKU列表
     */
    @NotEmpty(message = "SKU不能为空")
    @Valid
    private List<SkuDTO> skus;
}
