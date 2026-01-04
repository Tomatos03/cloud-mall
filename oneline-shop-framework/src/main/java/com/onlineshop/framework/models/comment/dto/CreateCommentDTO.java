package com.onlineshop.framework.models.comment.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建商品评论 DTO（数据传输对象）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCommentDTO {
    
    /**
     * 订单明细ID（必需）
     */
    @NotNull(message = "订单明细ID不能为空")
    private Long orderItemId;
    
    /**
     * 订单ID（必需）
     */
    @NotNull(message = "订单ID不能为空")
    private String orderNo;
    
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
    
    /**
     * 评论图片，逗号分隔URL（可选）
     */
    private String images;
    
    /**
     * 是否匿名：0-否 1-是（可选，默认0）
     */
    private Integer isAnonymous = 0;

    private Long userId;

    /**
     * 用户昵称（冗余，便于二次查询）
     */
    private String userName;
    
    /**
     * 用户头像（冗余，便于二次查询）
     */
    private String userAvatar;
}
