package com.onlineshop.framework.models.audit.enums;

import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.exception.BizException;
import com.onlineshop.framework.models.goods.spu.Goods;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.stream.Stream;

/**
 * 审核对象类型枚举
 * 
 * 注：code 值用于 JSON 反序列化和 Jackson 多态类型识别
 */
@Getter
@AllArgsConstructor
public enum AuditType {
    /**
     * 商品(SPU)
     */
    GOODS(AuditType.GOODS_CODE, "商品"),
    /**
     * 店铺注册
     */
    STORE_REGISTER(AuditType.STORE_REGISTER_CODE, "店铺注册"),
    /**
     * 秒杀活动
     */
    SECKILL_ACTIVITY(AuditType.SECKILL_ACTIVITY_CODE, "秒杀活动")
    ;
    
    public static final String GOODS_CODE = "GOODS";
    public static final String STORE_REGISTER_CODE = "STORE_REGISTER";
    public static final String SECKILL_ACTIVITY_CODE = "SECKILL_ACTIVITY";

    private final String code;
    private final String name;

    public static AuditType of(String code) {
        return Stream.of(values())
                .filter(type -> type.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new BizException(BizErrorCode.UNSUPPORTED_AUDIT_TYPE));
    }
}