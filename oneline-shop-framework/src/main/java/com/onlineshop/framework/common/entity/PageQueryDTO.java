package com.onlineshop.framework.common.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 公共分页查询基类
 * 子类可通过继承来扩展特定模块的分页查询条件
 *
 * @author : Tomatos
 * @date : 2026/1/24
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PageQueryDTO {
    /**
     * 当前页码（从1开始）
     */
    @JsonProperty("page")
    @Builder.Default
    private Long pageNo = 1L;

    /**
     * 每页数量
     */
    @Builder.Default
    private Long pageSize = 10L;
}
