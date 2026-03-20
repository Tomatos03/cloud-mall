package com.onlineshop.framework.models.goods.spu.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/1/20
 */
@Builder
@AllArgsConstructor
@NotBlank
@Data
public class WebSpuVO {
    private Long id; // 商品ID
    private String goodsName; // 商品名称
    private String sellPoint; // 商品简短信息
    private List<String> displayImageUrls; // 商品展示图片列表
    private List<String> descriptionImageUrls; // 商品描述图片列表
    private LocalDateTime createTime; // 上架时间或创建时间
    private Integer sale; // 总销量
    private String positiveRate; // 好评率
}
