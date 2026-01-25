package com.onlineshop.framework.models.store.dto;

import com.onlineshop.framework.common.entity.PageQueryDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 店铺商品查询 DTO
 * 用于分页查询店铺商品
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StoreGoodsQueryDTO extends PageQueryDTO {
    /**
     * 店铺ID
     */
    private Long storeId;
}
