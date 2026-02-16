package com.onlineshop.framework.models.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 审核提交DTO - 用于提交审核申请
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditSubmit {
    /**
     * 被审核对象类型: GOODS / SKU / OTHER
     */
    private String targetType;

    /**
     * 被审核对象ID
     */
    private Long targetId;

    /**
     * 扩展信息: 可存储SKU组合/商品规格等JSON
     */
    private String snapShot;
}