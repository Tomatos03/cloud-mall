package com.onlineshop.framework.models.goods.sku;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.onlineshop.framework.common.entity.PageParamsDTO;

/**
 * 商家SKU分页查询参数
 *
 * @author Tomatos
 * @date 2026/3/10
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MerchantGoodsSkuParamsDTO extends PageParamsDTO {

    /**
     * 商品ID（可选）
     */
    private Long goodsId;
}
