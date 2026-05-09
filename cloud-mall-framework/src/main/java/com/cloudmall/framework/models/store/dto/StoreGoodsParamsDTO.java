package com.cloudmall.framework.models.store.dto;

import com.cloudmall.framework.common.entity.PageParamsDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 店铺商品查询 DTO
 * 用于分页查询店铺商品
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StoreGoodsParamsDTO extends PageParamsDTO {
    /**
     * 店铺ID
     */
    private Long storeId;
}
