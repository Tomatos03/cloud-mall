package com.onlineshop.framework.models.order.dto;

import com.onlineshop.framework.common.entity.PageQueryDTO;
import com.onlineshop.framework.models.order.enums.OrderStatus;
import com.onlineshop.framework.models.order.enums.OrderType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Date;

/**
 * 订单查询 DTO
 * 支持用户端和管理端的分页查询
 *
 * @author : Tomatos
 * @date : 2025/12/24
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderQueryDTO extends PageQueryDTO {

    // ========== 查询条件 ==========
    /**
     * 订单号（模糊查询）
     */
    private String orderNo;

    /**
     * 店铺ID（管理端使用）
     */
    private Long storeId;

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
     * 开始时间（订单创建时间）
     */
    private Date startTime;
    
    /**
     * 结束时间（订单创建时间）
     */
    private Date endTime;
    
    /**
     * 父订单ID（查询某个父订单下的所有子订单）
     */
    private Long parentId;
    
    /**
     * 用户手机号（管理端使用，模糊查询）
     */
    private String userPhone;
    
    /**
     * 收货人姓名（管理端使用，模糊查询）
     */
    private String userName;
}