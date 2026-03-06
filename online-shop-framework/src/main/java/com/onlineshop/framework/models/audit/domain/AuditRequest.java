package com.onlineshop.framework.models.audit.domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.onlineshop.framework.models.audit.enums.AuditType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 审核请求基类
 * 定义所有业务审核请求的共同字段
 * 
 * 使用 Jackson 多态序列化，根据 type 字段自动识别实现类
 * type 值来自 AuditType 枚举的 code 常量
 *
 * @author Tomatos
 * @date 2026/2/26
 */
@Data
@EqualsAndHashCode
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type",
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = GoodsAuditRequest.class, name = AuditType.GOODS_CODE),
        @JsonSubTypes.Type(value = StoreRegisterAuditRequest.class, name = AuditType.STORE_REGISTER_CODE),
        @JsonSubTypes.Type(value = SeckillGoodsAuditRequest.class, name = AuditType.SECKILL_ACTIVITY_CODE)
})
public abstract class AuditRequest {

    /**
     * 业务类型：GOODS, STORE_REGISTER, SECKILL_ACTIVITY
     * 用于在运行时识别审核请求的业务类型
     */
    private String type;
    
    /**
     * 申请人ID
     */
    private Long applicantId;
    
    /**
     * 申请人名称
     */
    private String applicantName;
    
    /**
     * 被审核对象ID
     * 新增时可能为null或0
     */
    private Long targetId;
}