package com.onlineshop.framework.models.audit.domain;

import com.onlineshop.framework.models.goods.sku.SkuDTO;
import com.onlineshop.framework.models.goods.spec.dto.SpecificationsDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 商品审核请求
 * 包含商品提交审核时需要的所有信息
 *
 * @author Tomatos
 * @date 2026/2/26
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GoodsAuditRequest extends AuditRequest {
    /**
     * 商品ID (更新商品时使用，新增时为null)
     */
    private Long goodsId;

    /**
     * 商品名称
     */
    private String goodsName;
    
    /**
     * 分类ID
     */
    private Long categoryId;
    
    /**
     * 分类ID路径
     */
    private String categoryIdPath;
    
    /**
     * 单位ID
     */
    private Long unitId;
    
    /**
     * 单位名称
     */
    private String unitName;
    
    /**
     * 商品卖点
     */
    private String sellPoint;
    
    /**
     * 展示图URL列表 (1-5张)
     */
    private List<String> displayImageUrls;
    
    /**
     * 描述图URL列表 (1-8张)
     */
    private List<String> descriptionImageUrls;
    
    /**
     * 店铺ID
     */
    private Long storeId;
    
    /**
     * 店铺名称
     */
    private String storeName;
    
    /**
     * 商品状态 (1=上架，0=下架)
     */
    private Boolean status;
    
    /**
     * 审核状态
     */
    private String auditStatus;
    
    /**
     * 规格列表 (1-3个)
     */
    private List<SpecificationsDTO> specifications;
    
    /**
     * SKU列表
     */
    private List<SkuDTO> skus;
}
