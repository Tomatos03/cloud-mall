package com.cloudmall.framework.common.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 公共分页查询基类
 * 子类可通过继承来扩展特定模块的分页查询条件
 *
 * @author : Tomatos
 * @date : 2026/1/24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageParamsDTO {
    /**
     * 当前页码（从1开始）
     */
    private Integer page = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 10;
}
