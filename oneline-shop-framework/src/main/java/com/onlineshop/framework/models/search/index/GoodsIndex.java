package com.onlineshop.framework.models.search.index;

import com.onlineshop.framework.models.goods.spu.Goods;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

/**
 * 商品索引文档
 * 用于Elasticsearch索引存储商品信息，支持全文搜索和过滤
 *
 * @author : Tomatos
 * @date : 2025/1/1
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "goods-index", createIndex = true)
public class GoodsIndex {

    /**
     * 商品ID，作为ES文档的唯一标识
     */
    @Id
    private Long id;

    /**
     * 商品名称，支持分词搜索
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String name;

    /**
     * 商品描述，支持分词搜索
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String sellPoint;

    /**
     * 分类ID，用于精确查询和聚合
     */
    @Field(type = FieldType.Long)
    private List<Long> categoryPathIds;

    /**
     * 店铺ID，用于店铺筛选
     */
    @Field(type = FieldType.Long)
    private Long storeId;

    /**
     * 店铺名称
     */
    @Field(type = FieldType.Keyword)
    private String storeName;

    /**
     * 最小价格（单位：分），用于价格排序和范围查询
     */
    @Field(type = FieldType.Long)
    private Long minPrice;

    /**
     * 最大价格（单位：分），用于价格排序和范围查询
     */
    @Field(type = FieldType.Long)
    private Long maxPrice;

    /**
     * 销量，用于热销排序
     */
    @Field(type = FieldType.Integer)
    private Integer sales;

    /**
     * 商品状态（true=上架，false=下架）
     */
    @Field(type = FieldType.Boolean)
    private Boolean status;

    /**
     * 创建时间，用于新品排序
     */
    @Field(type = FieldType.Long)
    private Long createTime;

    /**
     * 商品展示图片URL，逗号分隔
     */
    @Field(type = FieldType.Keyword)
    private String displayImages;

    /**
     * 将Goods对象转换为GoodsIndex对象
     *
     * @param goods 商品对象
     * @return 商品索引对象
     */
    public static GoodsIndex convertToGoodsIndex(@NonNull Goods goods) {
        return GoodsIndex.builder()
                         .id(goods.getId())
                         .name(goods.getName())
                         .sellPoint(goods.getSellPoint())
                         .categoryPathIds(extractCategoryPathIds(goods.getCategoryIdPath()))
                         .storeId(goods.getStoreId())
                         .storeName(goods.getStoreName())
                         .minPrice(goods.getMinPrice())
                         .maxPrice(goods.getMaxPrice())
                         .sales(goods.getSales())
                         .status(goods.getStatus())
                         .displayImages(goods.getDisplayImages())
                         .createTime(
                                 goods.getCreateTime()
                                      .atZone(ZoneId.systemDefault())
                                      .toInstant()
                                      .toEpochMilli()
                         )
                         .build();
    }

    /**
     * 从分类路径中提取顶级分类ID
     * 分类路径格式为: 1,2,3 (从顶级到当前分类)
     *
     * @param categoryIdPath 分类路径
     * @return 顶级分类ID，如果路径为空返回null
     */
    private static List<Long> extractCategoryPathIds(String categoryIdPath) {
        if (categoryIdPath == null || categoryIdPath.trim()
                                                    .isEmpty()) {
            return null;
        }
        return Arrays.stream(categoryIdPath.split("/"))
                     .map(Long::valueOf)
                     .toList();
    }
}