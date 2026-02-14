package com.onlineshop.framework.models.system.resource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 资源元信息，对应数据库 resources 表的 meta_json 字段
 * 包含前端路由相关的配置信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceMeta {
    private String icon;
    private String label;
    private String name;
    private String redirect;
    private String component;
    private String path;
}