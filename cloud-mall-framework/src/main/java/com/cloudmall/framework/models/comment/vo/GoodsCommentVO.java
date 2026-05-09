package com.cloudmall.framework.models.comment.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品评论 VO（视图对象）- 用于返回给前端
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoodsCommentVO {

    /**
     * 评论ID
     */
    private Long id;

    /**
     * 订单明细ID
     */
    private Long orderItemId;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 商品ID
     */
    private Long goodsId;

    /**
     * 商品名称（冗余字段，便于二次查询）
     */
    private String goodsName;

    /**
     * 商品图片（冗余字段，便于二次查询）
     */
    private String goodsImage;

    /**
     * 评论用户ID
     */
    private Long userId;

    /**
     * 用户昵称（冗余字段，便于商品详细页展示用户名称)
     */
    private String userNickname;

    /**
     * 用户头像（冗余字段，便于商品详细页展示用户头像)
     */
    private String userAvatar;

    /**
     * 评分：1~5
     */
    private Integer rating;

    /**
     * 评论内容
     */
    private String content;

    private String reply;

    /**
     * 评论图片列表
     */
    private List<String> imageUrls;

    /**
     * 是否匿名：0-否 1-是
     */
    private Boolean isAnonymous;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    @JsonProperty("specs")
    private String specSnapshot;
}