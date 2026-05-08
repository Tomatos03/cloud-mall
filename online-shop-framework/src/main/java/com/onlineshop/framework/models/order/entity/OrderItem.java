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
 * 订单明细实体类
 * 支持多规格商品：通过 skuSpecs 字段存储购买时选择的规格信息
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
    private Long skuId;        // SKU ID（真实下单对象，支持多规格商品）
    private Long goodsId;      // 商品ID
    private String goodsName;  // 商品名称快照
    private String goodsMainImageUrl;
    private Long goodsPrice;   // 下单时商品单价（单位：分）
    private Integer quantity;     // 购买数量
    private Long totalPrice;   // 明细小计（单位：分）
    private Long originalPrice;   // 折扣前价格（分），= goodsPrice × quantity
    private Long discountAmount;  // 分摊到该商品的优惠（分）
    private String skuSpecs;   // SKU规格快照，格式：颜色=黑色;尺码=L（多规格值用;分隔）
    private LocalDateTime createTime;   // 创建时间
    private Boolean commentStatus; // 评价状态：0-未评价 1-已评价
}