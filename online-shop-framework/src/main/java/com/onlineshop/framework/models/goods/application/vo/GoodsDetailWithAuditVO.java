package com.onlineshop.framework.models.goods.application.vo;

import com.onlineshop.framework.models.goods.sku.SkuDTO;
import com.onlineshop.framework.models.goods.spec.dto.SpecificationsDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 商品详情信息VO（包含审核信息）
 * 用于返回给商家端的商品详情，包含描述图片、规格、SKU和审核信息
 *
 * @author Tomatos
 * @date 2026/2/12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsDetailWithAuditVO {

    /**
     * 商品描述图片URL列表
     */
    private List<String> descriptionImageUrls;

    /**
     * 商品规格列表
     */
    private List<SpecificationsDTO> specifications;

    /**
     * 商品SKU列表
     */
    private List<SkuDTO> skus;

    /**
     * 审核信息（如果存在待审核或已拒绝状态）
     */
    private AuditGoodsVO auditInfo;
}

