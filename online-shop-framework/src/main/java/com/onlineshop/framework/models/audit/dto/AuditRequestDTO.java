package com.onlineshop.framework.models.audit.dto;

import com.onlineshop.framework.models.audit.domain.AuditRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一的审核请求DTO
 * 使用泛型包装所有类型的审核请求，提供统一的API入口
 * 
 * 核心作用：
 * 1. 所有审核请求都通过这个DTO传入，无论是商品审核、店铺注册审核还是秒杀活动审核
 * 2. businessType字段用于在运行时识别具体是哪种审核，工厂模式使用
 * 3. data字段包含具体的审核请求对象（可能是GoodsAuditRequest、StoreRegisterAuditRequest等）
 * 
 * 使用示例：
 * AuditRequestDTO<GoodsAuditRequest> dto = AuditRequestDTO.builder()
 *     .businessType("GOODS")
 *     .data(goodsAuditRequest)
 *     .build();
 *
 * @param <T> 审核请求类型，必须继承AuditRequest
 * @author Tomatos
 * @date 2026/2/26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditRequestDTO<T extends AuditRequest> {
    
    /**
     * 业务类型：GOODS, STORE_REGISTER, SECKILL_ACTIVITY等
     * 此字段用于工厂模式，根据businessType获取对应的Auditor子类
     */
    @NotBlank(message = "业务类型不能为空")
    private String businessType;
    
    /**
     * 具体的审核请求对象（泛型）
     * 包含特定业务的详细审核信息
     */
    @NotNull(message = "审核请求数据不能为空")
    @Valid
    private T data;
}
