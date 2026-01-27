package com.onlineshop.framework.models.order.enums;

import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 父订单状态枚举
 * 用于聚合父订单整体流程控制
 *
 * @author : Tomatos
 * @date : 2025/12/21
 */
@Getter
@AllArgsConstructor
public enum ParentOrderStatus {
    /**
     * 待支付
     */
    CREATED("CREATED", "待支付"),

    /**
     * 已支付
     */
    PAID("PAID", "已支付"),

    /**
     * 履约中（有子订单正在履约，未全部完成）
     */
    PROCESSING("PROCESSING", "履约中"),

    /**
     * 已完成（所有子订单均已完成）
     */
    FINISHED("FINISHED", "已完成"),

    /**
     * 已取消（父订单整体取消，不存在部分取消）
     */
    CANCELED("CANCELED", "已取消"),

    /**
     * 已关闭（父订单整体关闭，不存在部分关闭）
     */
    CLOSED("CLOSED", "已关闭");

    private final String code;
    private final String desc;

    public static ParentOrderStatus of(String code) {
        return Arrays.stream(values())
                .filter(status -> status.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new BusinessException(BizErrorCode.INVALID_ORDER_STATUS));
    }
}