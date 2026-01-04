package com.onlineshop.framework.models.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 *
 *
 * @author : Tomatos
 * @date : 2025/12/24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("order_item")
public class OrderItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;      // 订单ID（关联orders.id）
    private Long goodsId;      // 商品ID
    private String goodsName;  // 商品名称快照
    private String goodsImg;   // 商品图片快照
    private Long goodsPrice;   // 下单时商品单价（单位：分）
    private Integer quantity;     // 购买数量
    private Long totalPrice;   // 明细小计（单位：分）
    private LocalDateTime createTime;   // 创建时间
    private Integer commentStatus; // 评价状态
}