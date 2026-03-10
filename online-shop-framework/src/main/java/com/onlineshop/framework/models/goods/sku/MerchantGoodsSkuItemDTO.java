package com.onlineshop.framework.models.goods.sku;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商家SKU分页项
 *
 * @author Tomatos
 * @date 2026/3/10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantGoodsSkuItemDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * SKU ID
     */
    private String skuId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品主图
     */
    private String imageUrl;

    /**
     * 价格（元）
     */
    private String price;

    /**
     * 规格值数组（如：红、XL）
     */
    private List<String> specs;

    /**
     * 库存
     */
    private Long inventory;
}
