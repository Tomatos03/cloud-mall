package com.cloudmall.framework.models.audit.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 秒杀活动审核项目DTO
 * 代表一个审核批次中的单个秒杀商品审核项目
 * <p>
 * 设计说明：
 * - 对应 AuditItem 表，存储在 snapshot 字段中
 * - 批次级活动信息由 Audit.bizPid 表示
 * - 审核员基于此信息做出批准或拒绝决策
 *
 * @author Tomatos
 * @date 2026/3/7
 */
@Data
public class SeckillGoodsAuditItemDTO {
    /**
     * SKU ID（秒杀申请绑定具体 SKU）
     */
    @NotNull
    private Long skuId;

    /**
     * 商品名称（提交审核时自动填充）
     */
    private String goodsName;

    /**
     * 商品主图（提交审核时自动填充）
     */
    private String mainImageUrl;

    private String specSnapshot; // 规格快照（提交审核时自动填充）

    private Long storeId;

    private Long originPrice;

    /**
     * 秒杀价格
     */
    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal seckillPrice;

    /**
     * 秒杀库存
     */
    @NotNull
    @Min(value = 1)
    private Integer stock;
}
