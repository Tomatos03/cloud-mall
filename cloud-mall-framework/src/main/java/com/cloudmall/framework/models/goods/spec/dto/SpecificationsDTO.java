package com.cloudmall.framework.models.goods.spec.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;




/**
 * 规格DTO
 *
 * @author Tomatos
 * @date 2026/1/12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecificationsDTO {
    /**
     * 规格名（如："颜色"）
     */
    @NotBlank(message = "规格名不能为空")
    private String name;

    /**
     * 规格值数组（如：["红色", "蓝色"]）
     */
    @NotEmpty(message = "规格值不能为空")
    private List<String> values;
}
