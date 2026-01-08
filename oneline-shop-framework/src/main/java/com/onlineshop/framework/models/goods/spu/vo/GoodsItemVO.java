package com.onlineshop.framework.models.goods.spu.vo;

import lombok.Data;

import java.util.List;

/**
 * 商品详情VO（用于编辑模式）
 *
 * @author Tomatos
 * @date 2026/1/6
 */
@Data
public class GoodsItemVO {
    
    /**
     * 商品ID
     */
    private Long id;

    /**
     * 商品名称
     */
    private String name;
    
    /**
     * 分类ID
     */
    private String categoryId;
    
    /**
     * 单位
     */
    private String unit;
    
    /**
     * 商品简介
     */
    private String info;
    
    /**
     * 主图URL
     */
    private String img;
    
    /**
     * 展示图URL列表
     */
    private String imgList;
    
    /**
     * 详情图URL列表
     */
    private String detailImages;
    
    /**
     * 店铺ID
     */
    private String storeId;
    
    /**
     * 店铺名称
     */
    private String storeName;
    
    /**
     * 状态：1=上架，0=下架
     */
    private Integer status;
    
    /**
     * 规格列表
     */
    private List<GoodsSpecification> specifications;
    
    /**
     * SKU列表
     */
    private List<GoodsSkuItem> skus;
    
    /**
     * 商品规格信息
     */
    @Data
    public static class GoodsSpecification {
        /**
         * 规格ID
         */
        private Long specId;
        
        /**
         * 规格名
         */
        private String name;
        
        /**
         * 规格值列表
         */
        private List<GoodsSpecValue> values;
    }
    
    /**
     * 规格值信息
     */
    @Data
    public static class GoodsSpecValue {
        /**
         * 规格值ID
         */
        private Long specValueId;
        
        /**
         * 规格值
         */
        private String value;
    }
    
    /**
     * 商品SKU信息
     */
    @Data
    public static class GoodsSkuItem {
        /**
         * SKU ID
         */
        private Long skuId;
        
        /**
         * 规格组合
         */
        private List<SkuSpec> specs;
        
        /**
         * 价格（分为单位）
         */
        private Long price;
        
        /**
         * 库存
         */
        private Long inventory;
        
        /**
         * 状态
         */
        private Integer status;
    }
    
    /**
     * SKU规格信息
     */
    @Data
    public static class SkuSpec {
        /**
         * 规格ID
         */
        private Long specId;
        
        /**
         * 规格名
         */
        private String name;
        
        /**
         * 规格值ID
         */
        private Long specValueId;
        
        /**
         * 规格值
         */
        private String value;
    }
}