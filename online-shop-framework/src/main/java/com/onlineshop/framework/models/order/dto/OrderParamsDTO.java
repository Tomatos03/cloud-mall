package com.onlineshop.framework.models.order.dto;

import com.onlineshop.framework.common.entity.PageParamsDTO;
import com.onlineshop.framework.models.order.enums.OrderStatus;
import com.onlineshop.framework.models.order.enums.OrderType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 订单查询 DTO
 * 支持用户端和管理端的分页查询
 *
 * @author : Tomatos
 * @date : 2025/12/24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderParamsDTO extends PageParamsDTO {

    /**
     * 订单号（模糊查询）
     */
    private String orderNo;

    /**
     * 订单状态
     * @see OrderStatus
     */
    private String status;
    
    /**
     * 订单类型：PARENT-父订单, SUB-子订单, NORMAL-普通订单
     * @see OrderType
     */
    private String orderType;

    /**
     * 父订单ID（查询某个父订单下的所有子订单）
     */
    private Long parentId;
}