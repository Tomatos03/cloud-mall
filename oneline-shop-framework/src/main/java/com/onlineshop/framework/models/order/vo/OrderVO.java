package com.onlineshop.framework.models.order.vo;

import com.onlineshop.framework.models.order.entity.Order;
import com.onlineshop.framework.utils.MoneyUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 订单视图对象
 * 只负责数据转换，查询条件构建已迁移到 OrderQueryWrapper
 *
 * @author : Tomatos
 * @date : 2025/12/25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderVO {
    private String orderNo;
    private String orderStatus;
    private String orderType;
    private LocalDateTime createTime;
    private Integer goodsNum;
    private Long totalPrice;
    private String totalPriceText;
    private String buyerName;
    private Integer regionCode;
    private String detailAddress;
    private String zipCode;
    private String phone;

    /**
     * 将Order实体转换为OrderVO
     *
     * @param order 订单实体
     * @return 订单视图对象
     */
    public static OrderVO buildOrderVO(Order order) {
        Objects.requireNonNull(order);

        return OrderVO.builder()
                      .orderNo(order.getNo())
                      .orderStatus(order.getStatus())
                      .createTime(order.getCreateTime())
                      .goodsNum(order.getQuantity())
                      .totalPrice(order.getTotalPrice())
                      .totalPriceText(MoneyUtil.fenToYuan(order.getTotalPrice()))
                      .buyerName(order.getUserName())
                      .phone(order.getPhone())
                      .detailAddress(order.getAddress())
                      .orderType(order.getOrderType())
                      .build();
    }
}