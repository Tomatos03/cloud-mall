package com.onlineshop.framework.models.seckill.enums;

/**
 * 秒杀活动状态枚举
 */
public enum SeckillStatusEnum {
    
    /**
     * 未开始
     */
    NOT_STARTED(0, "未开始"),
    
    /**
     * 进行中
     */
    ONGOING(1, "进行中"),
    
    /**
     * 已结束
     */
    ENDED(2, "已结束");
    
    private final Integer code;
    private final String desc;
    
    SeckillStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public String getDesc() {
        return desc;
    }
    
    public static SeckillStatusEnum getByCode(Integer code) {
        for (SeckillStatusEnum status : SeckillStatusEnum.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}