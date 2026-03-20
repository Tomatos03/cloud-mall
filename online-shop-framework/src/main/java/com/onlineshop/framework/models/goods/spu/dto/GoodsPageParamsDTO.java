package com.onlineshop.framework.models.goods.spu.dto;

import com.onlineshop.framework.common.entity.PageParamsDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品分页查询参数
 *
 * @author : Tomatos
 * @date : 2026/3/20
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GoodsPageParamsDTO extends PageParamsDTO {

    /**
     * 商品状态：true=上架，false=下架
     */
    private Boolean status;

    /**
     * 当前商家店铺ID（由登录态注入）
     */
    private Long storeId;
}
