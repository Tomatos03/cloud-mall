package com.onlineshop.framework.models.comment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 创建商品评论 DTO（数据传输对象）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCommentDTO {
    @NotNull
    private String orderNo;

    /**
     * 订单明细ID（必需）
     */
    @NotNull(message = "订单明细ID不能为空")
    private Long orderItemId;

    /**
     * 商品ID（必需）
     */
    @NotNull(message = "商品ID不能为空")
    private Long goodsId;

    /**
     * 评分：1~5（必需）
     */
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低为1分")
    @Max(value = 5, message = "评分最高为5分")
    private Integer rating;
    
    /**
     * 评论内容（必需）
     */
    @NotBlank(message = "评论内容不能为空")
    private String content;

    private List<String> imageUrls;

    /**
     * 是否匿名：0-否 1-是（可选，默认0）
     */
    private Boolean isAnonymous = false;

    // 商品规格快照
    @NotBlank
    @JsonProperty("specs")
    private String specSnapshot;
}
