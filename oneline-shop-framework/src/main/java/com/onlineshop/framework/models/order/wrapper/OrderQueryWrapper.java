package com.onlineshop.framework.models.order.wrapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.onlineshop.framework.enums.BizErrorCode;
import com.onlineshop.framework.exception.BusinessException;
import com.onlineshop.framework.models.order.dto.OrderQueryDTO;
import com.onlineshop.framework.models.order.entity.Order;
import com.onlineshop.framework.models.order.enums.OrderType;
import com.onlineshop.framework.models.store.Store;
import com.onlineshop.framework.models.user.UserRole;
import com.onlineshop.framework.utils.context.UserContextHolder;
import io.micrometer.common.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 订单查询条件构建器
 * 负责构建订单查询的 QueryWrapper，包含权限过滤和通用查询条件
 *
 * @author : Tomatos
 * @date : 2025/12/25
 */
public class OrderQueryWrapper {

    /**
     * 构建订单查询条件（包含权限过滤和查询条件）
     *
     * @param role      当前用户角色
     * @param queryDTO  查询条件DTO
     * @param storeList 用户关联的店铺列表（商家角色时需要）
     * @return 查询条件wrapper
     */
    public static LambdaQueryWrapper<Order> build(
            UserRole role,
            OrderQueryDTO queryDTO,
            List<Store> storeList
    ) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        fillByUserRole(wrapper, role, storeList);
        fillCommonConditions(wrapper, queryDTO);
        return wrapper;
    }

    /**
     * 构建权限过滤条件
     *
     * @param wrapper   查询条件wrapper
     * @param role      用户角色
     * @param storeList 店铺列表
     */
    private static void fillByUserRole(
            LambdaQueryWrapper<Order> wrapper,
            UserRole role,
            List<Store> storeList
    ) {
        switch (role) {
            case NORMAL -> fillUserQueryWrapper(wrapper);
            case MERCHANT -> fillMerchantQueryWrapper(wrapper, storeList);
            case ADMIN -> fillAdminQueryWrapper(wrapper);
            default -> throw new BusinessException(BizErrorCode.UNKNOWN_ROLE);
        };
    }

    private static void fillUserQueryWrapper(LambdaQueryWrapper<Order> wrapper) {
        wrapper.eq(Order::getUserId, UserContextHolder.getUserId());
        wrapper.in(Order::getOrderType, OrderType.NORMAL.getCode(), OrderType.PARENT.getCode());
    }

    private static void fillMerchantQueryWrapper(LambdaQueryWrapper<Order> wrapper, List<Store> storeList) {
        Objects.requireNonNull(storeList);
        List<Long> storeIdList = storeList.stream()
                                          .map(Store::getId)
                                          .collect(Collectors.toList());
        if (storeIdList.isEmpty()) {
            throw new BusinessException(BizErrorCode.MERCHANT_NO_SHOP);
        }

        wrapper.in(Order::getStoreId, storeIdList);
        wrapper.in(Order::getOrderType, OrderType.NORMAL.getCode(), OrderType.SUB.getCode());
    }

    private static void fillAdminQueryWrapper(LambdaQueryWrapper<Order> wrapper) {
        wrapper.in(Order::getOrderType, OrderType.NORMAL.getCode(), OrderType.PARENT.getCode());
    }

    /**
     * 构建通用查询条件（不包含权限过滤）
     *
     * @param wrapper  查询条件wrapper
     * @param queryDTO 查询条件DTO
     */
    private static void fillCommonConditions(
            LambdaQueryWrapper<Order> wrapper,
            OrderQueryDTO queryDTO
    ) {
        // 订单号（模糊查询）
        wrapper.like(
                StringUtils.isNotBlank(queryDTO.getOrderNo()),
                Order::getNo,
                queryDTO.getOrderNo()
        );

        // 订单状态
        wrapper.eq(
                StringUtils.isNotBlank(queryDTO.getStatus()),
                Order::getStatus,
                queryDTO.getStatus()
        );

        // 订单类型
        wrapper.eq(
                StringUtils.isNotBlank(queryDTO.getOrderType()),
                Order::getOrderType,
                queryDTO.getOrderType()
        );

        // 父订单ID
        wrapper.eq(
                queryDTO.getParentId() != null,
                Order::getParentId,
                queryDTO.getParentId()
        );

        // 时间范围查询
        wrapper.ge(
                queryDTO.getStartTime() != null,
                Order::getCreateTime,
                queryDTO.getStartTime()
        );
        wrapper.le(
                queryDTO.getEndTime() != null,
                Order::getCreateTime,
                queryDTO.getEndTime()
        );

        // 收货人手机号（模糊查询）
        wrapper.like(
                StringUtils.isNotBlank(queryDTO.getUserPhone()),
                Order::getPhone,
                queryDTO.getUserPhone()
        );

        // 收货人姓名（模糊查询）
        wrapper.like(
                StringUtils.isNotBlank(queryDTO.getUserName()),
                Order::getUserName,
                queryDTO.getUserName()
        );
    }
}