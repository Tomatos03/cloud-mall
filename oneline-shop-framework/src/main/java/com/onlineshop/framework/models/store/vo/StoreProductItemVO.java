package com.onlineshop.framework.models.store.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 店铺商品列表项 VO
 */
@Data
@Builder
public class StoreProductItemVO implements Serializable {
    /**
     * 商品唯一标识 ID
     */
    private String id;

    /**
     * 商品标题
     */
    private String title;

    /**
     * 商品简短描述/副标题
     */
    private String desc;

    /**
     * 商品价格（建议后端格式化好或传分单位由前端处理）
     */
    private Long price;

    /**
     * 商品主图 URL
     */
    private String img;

    /**
     * 销量（可选）
     */
    private Integer sale;
}