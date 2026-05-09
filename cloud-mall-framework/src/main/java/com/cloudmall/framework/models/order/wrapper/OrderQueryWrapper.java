package com.cloudmall.framework.models.order.wrapper;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudmall.framework.common.enums.BizErrorCode;
import com.cloudmall.framework.models.auth.enums.AccountType;
import com.cloudmall.framework.models.order.dto.OrderParamsDTO;
import com.cloudmall.framework.models.order.entity.Order;
import com.cloudmall.framework.models.order.enums.OrderType;
import com.cloudmall.framework.utils.AssertUtils;
import com.cloudmall.framework.utils.AuthUserUtils;

/**
 * 订单查询条件构建器
 * 负责构建订单分页查询的 QueryWrapper
 */
public class OrderQueryWrapper {

    private OrderQueryWrapper() {
    }

    /**
     * 按当前登录账号类型构建查询条件
     */
    public static LambdaQueryWrapper<Order> build(OrderParamsDTO queryDTO) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        fillByAccountType(wrapper);
        fillCommonConditions(wrapper, queryDTO);
        wrapper.orderByDesc(Order::getCreateTime);
        return wrapper;
    }

    private static void fillByAccountType(LambdaQueryWrapper<Order> wrapper) {
        AccountType accountType = AuthUserUtils.getAccountType();

        switch (accountType) {
            case NORMAL -> fillUserQueryWrapper(wrapper);
            case MERCHANT -> fillMerchantQueryWrapper(wrapper);
            case ADMIN -> fillAdminQueryWrapper(wrapper);
        }
    }

    private static void fillUserQueryWrapper(LambdaQueryWrapper<Order> wrapper) {
        wrapper.eq(Order::getUserId, AuthUserUtils.getUserId());
        wrapper.in(Order::getOrderType, OrderType.NORMAL.getCode(), OrderType.PARENT.getCode());
    }

    private static void fillMerchantQueryWrapper(LambdaQueryWrapper<Order> wrapper) {
        Long storeId = AuthUserUtils.getStoreId();
        AssertUtils.notNull(storeId, BizErrorCode.MERCHANT_NO_SHOP);

        wrapper.eq(Order::getStoreId, storeId);
        wrapper.in(Order::getOrderType, OrderType.NORMAL.getCode(), OrderType.SUB.getCode());
    }

    private static void fillAdminQueryWrapper(LambdaQueryWrapper<Order> wrapper) {
        wrapper.in(Order::getOrderType, OrderType.NORMAL.getCode(), OrderType.PARENT.getCode());
    }

    private static void fillCommonConditions(
            LambdaQueryWrapper<Order> wrapper,
            OrderParamsDTO queryDTO
    ) {
        wrapper.like(
                StrUtil.isNotBlank(queryDTO.getOrderNo()),
                Order::getNo,
                queryDTO.getOrderNo()
        );

        wrapper.eq(
                StrUtil.isNotBlank(queryDTO.getStatus()),
                Order::getStatus,
                queryDTO.getStatus()
        );

        wrapper.eq(
                StrUtil.isNotBlank(queryDTO.getOrderType()),
                Order::getOrderType,
                queryDTO.getOrderType()
        );

        wrapper.eq(
                queryDTO.getParentId() != null,
                Order::getParentId,
                queryDTO.getParentId()
        );
    }
}
