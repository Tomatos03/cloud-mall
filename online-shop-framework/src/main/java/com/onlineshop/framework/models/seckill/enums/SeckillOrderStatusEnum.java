package com.onlineshop.framework.models.seckill.enums;

/**
 * 秒杀订单状态枚举
 */
public enum SeckillOrderStatusEnum {
    
    /**
     * 待支付
     */
    PENDING_PAYMENT(0, "待支付"),
    
    /**
     * 已支付
     */
    PAID(1, "已支付"),
    
    /**
     * 已发货
     */
    SHIPPED(2, "已发货"),
    
    /**
     * 已完成
     */
    COMPLETED(3, "已完成"),
    
    /**
     * 已取消
     */
    CANCELLED(4, "已取消"),
    
    /**
     * 已退货
     */
    RETURNED(5, "已退货");

    private final Integer code;
    private final String desc;

    SeckillOrderStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static SeckillOrderStatusEnum getByCode(Integer code) {
        for (SeckillOrderStatusEnum status : SeckillOrderStatusEnum.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}