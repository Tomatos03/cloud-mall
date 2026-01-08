package com.onlineshop.framework.models.goods.spu.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 商品发布请求对象（新增/更新）
 *
 * @author Tomatos
 * @date 2026/1/6
 */
@Data
public class GoodsPublishPayload {
    private Long goodsId;
    /**
     * 商品名称
     */
    @NotBlank(message = "商品名称不能为空")
    private String name;
    
    /**
     * 分类ID（级联选择器的最后一级）
     */
    @NotNull(message = "分类ID不能为空")
    private Integer categoryId;
    
    /**
     * 单位名称，如："件"、"套"
     */
    @NotBlank(message = "单位不能为空")
    private String unit;
    
    /**
     * 商品简介
     */
    private String info;
    
    /**
     * 主图URL（第一张展示图）
     */
    @NotBlank(message = "主图不能为空")
    private String img;
    
    /**
     * 展示图URL列表（逗号分隔，不含主图）
     */
    private String imgList;
    
    /**
     * 详情图URL列表（逗号分隔）
     */
    private String detailImages;
    
    /**
     * 规格列表
     */
    @NotEmpty(message = "规格不能为空")
    @Valid
    private List<SpecificationDTO> specifications;
    
    /**
     * SKU列表
     */
    @NotEmpty(message = "SKU不能为空")
    @Valid
    private List<SkuDTO> skus;
    
    /**
     * 店铺ID
     */
    @NotBlank(message = "店铺ID不能为空")
    private String storeId;
    
    /**
     * 店铺名称
     */
    private String storeName;
    
    /**
     * 状态：1=上架，0=下架
     */
    @NotNull(message = "状态不能为空")
    private Integer status;
    
    /**
     * 规格DTO
     */
    @Data
    public static class SpecificationDTO {
        /**
         * 规格名（如："颜色"）
         */
        @NotBlank(message = "规格名不能为空")
        private String name;
        
        /**
         * 规格值数组（如：["红色", "蓝色"]）
         */
        @NotEmpty(message = "规格值不能为空")
        private List<String> values;
    }
    
    /**
     * SKU DTO
     */
    @Data
    public static class SkuDTO {
        /**
         * 规格信息
         */
        @NotEmpty(message = "SKU规格信息不能为空")
        @Valid
        private List<SpecValueDTO> specs;
        
        /**
         * 价格（整数，分为单位）
         */
        @NotNull(message = "SKU价格不能为空")
        private Long price;
        
        /**
         * 库存数量
         */
        @NotNull(message = "SKU库存不能为空")
        private Long inventory;
        
        /**
         * 启用状态
         */
        @NotNull(message = "SKU状态不能为空")
        private Integer status;
    }
    
    /**
     * 规格值DTO
     */
    @Data
    public static class SpecValueDTO {
        /**
         * 规格名
         */
        @NotBlank(message = "规格名不能为空")
        private String name;
        
        /**
         * 规格值
         */
        @NotBlank(message = "规格值不能为空")
        private String value;
    }
}