package com.cloudmall.framework.models.goods.spec.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 规格值DTO
 *
 * @author Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpeValueDTO {
    /**
     * 规格名(冗余字段)
     */
    @NotBlank(message = "规格名不能为空")
    private String name;

    /**
     * 规格值
     */
    @NotBlank(message = "规格值不能为空")
    private String value;
}

