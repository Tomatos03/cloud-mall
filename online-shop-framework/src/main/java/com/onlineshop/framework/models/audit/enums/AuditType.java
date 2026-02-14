package com.onlineshop.framework.models.audit.enums;

import com.onlineshop.framework.models.goods.spu.Goods;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.stream.Stream;

/**
 * 审核对象类型枚举
 */
@Getter
@AllArgsConstructor
public enum AuditType {
    /**
     * 商品(SPU)
     */
    GOODS("GOODS", "商品"),
    ;

    private final String code;
    private final String name;

    public static AuditType of(String code) {
        return Stream.of(values())
                .filter(type -> type.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未知的审核对象类型: " + code));
    }
}