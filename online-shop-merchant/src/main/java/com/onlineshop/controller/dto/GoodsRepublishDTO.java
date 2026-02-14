package com.onlineshop.controller.dto;

import com.onlineshop.framework.models.goods.application.GoodsDTO;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 重新发布商品请求DTO
 * 用于商家审核被撤销或拒绝后重新提交商品
 *
 * @author Tomatos
 * @date 2026/2/11
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsRepublishDTO extends GoodsDTO {
    /**
     * 被撤销的审核记录ID
     */
    @NotNull(message = "审核记录ID不能为空")
    private Long auditId;
}


