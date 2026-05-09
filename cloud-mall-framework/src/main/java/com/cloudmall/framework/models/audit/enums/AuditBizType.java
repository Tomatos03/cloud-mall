package com.cloudmall.framework.models.audit.enums;

import com.cloudmall.framework.common.enums.BizErrorCode;
import com.cloudmall.framework.exception.BizException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.stream.Stream;

/**
 * 审核对象类型枚举
 * 
 * 注：code 值用于 JSON 反序列化和 Jackson 多态类型识别
 */
@Getter
@AllArgsConstructor
public enum AuditBizType {
    /**
     * 商品(SPU)
     */
    GOODS(AuditBizType.GOODS_CODE, "商品"),
    /**
     * 店铺注册
     */
    STORE_REGISTER(AuditBizType.STORE_REGISTER_CODE, "店铺注册"),
    /**
     * 秒杀活动
     */
    SECKILL_GOODS(AuditBizType.SECKILL_GOODS_CODE, "秒杀活动")
    ;
    
    public static final String GOODS_CODE = "GOODS";
    public static final String STORE_REGISTER_CODE = "STORE_REGISTER";
    public static final String SECKILL_GOODS_CODE = "SECKILL_GOODS";

    private final String code;
    private final String name;

    public static AuditBizType of(String code) {
        return Stream.of(values())
                .filter(type -> type.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new BizException(BizErrorCode.UNSUPPORTED_AUDIT_TYPE));
    }
}