package com.cloudmall.framework.models.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.cloudmall.framework.models.order.enums.OrderStatus;
import com.cloudmall.framework.models.order.enums.OrderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 订单实体类
 */
@Data
@TableName("orders")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    /**
     * 订单ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 父订单ID（多店铺场景）
     */
    private Long parentId;
    
    /**
     * 订单号
     */
    private String no;

    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 商家ID（父订单为NULL）
     */
    private Long storeId;
    
    /**
     * 订单项数量（父订单表示子订单数量，子订单表示商品项数量）
     */
    private Integer quantity;
    
    /**
     * 订单总价（单位：分）
     */
    private Long totalPrice;
    
    /**
     * 下单用户名
     */
    private String userName;
    
    /**
     * 下单地址
     */
    private String address;
    
    /**
     * 下单电话
     */
    private String phone;

    /**
     * 订单状态
     */
    private String status;
    
    /**
     * 订单类型：PARENT-父订单, SUB-子订单, NORMAL-普通订单（单店铺）
     */
    private String orderType;

    private String reason;

    /**
     * 使用的 user_coupon.id
     */
    private Long couponId;

    /**
     * 该订单分摊到的优惠金额（分）
     */
    private Long couponDiscount;

    /**
     * 实付金额（分）= totalPrice - couponDiscount
     */
    private Long payAmount;

    /**
     * 下单时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    public boolean isParent() {
        return OrderType.of(orderType).isParent();
    }

    public boolean isExpired() {
        return this.getCreateTime().plusMinutes(30L).isAfter(LocalDateTime.now());
    }

    public boolean isCreated() {
        return OrderStatus.of(status) == OrderStatus.CREATED;
    }

    public boolean isPaid() {
        return OrderStatus.of(status) == OrderStatus.PAID;
    }

    public boolean isShipped() {
        return OrderStatus.of(status) == OrderStatus.SHIPPED;
    }

    public boolean isFinished() {
        return OrderStatus.of(status) == OrderStatus.FINISHED;
    }

    public boolean isCanceled() {
        return OrderStatus.of(status) == OrderStatus.CANCELED;
    }

    public boolean isClosed() {
        return OrderStatus.of(status) == OrderStatus.CLOSED;
    }
}