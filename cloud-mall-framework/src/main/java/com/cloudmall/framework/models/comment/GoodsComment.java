package com.cloudmall.framework.models.comment;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("goods_comment")
public class GoodsComment {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 订单明细ID（唯一，一次购买一次评价）
     */
    private Long orderItemId;
    
    /**
     * 订单ID（冗余，便于查询）
     */
    private Long orderId;
    
    /**
     * 商品ID
     */
    private Long goodsId;

    /**
     * 评论用户ID
     */
    private Long userId;
    
    /**
     * 用户昵称（冗余，便于二次查询）
     */
    private String userNickname;
    
    /**
     * 用户头像（冗余，便于二次查询）
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
    
    /**
     * 评论图片，逗号分隔URL
     */
    private String imageUrls;
    
    /**
     * 是否匿名：0-否 1-是
     */
    private Boolean isAnonymous;
    
    /**
     * 商家回复内容
     */
    private String reply;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    private String skuSpecSnapshot;
}