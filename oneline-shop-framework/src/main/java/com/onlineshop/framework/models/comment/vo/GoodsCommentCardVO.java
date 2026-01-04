package com.onlineshop.framework.models.comment.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *
 *
 * @author : Tomatos
 * @date : 2025/12/28
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GoodsCommentCardVO implements Serializable {
    private Long commentId;
    /**
     * 订单号（评论的唯一标识）
     */
    private String orderNo;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品图片URL
     */
    private String goodsImage;

    /**
     * 买家名称
     */
    private String buyerName;

    /**
     * 评分（1-5星）
     */
    private Integer rate;

    /**
     * 买家评价内容
     */
    private String comment;

    /**
     * 商家回复内容（可为空表示未回复）
     */
    private String reply;

    /**
     * 评论创建时间
     */
    private LocalDateTime createTime;
}