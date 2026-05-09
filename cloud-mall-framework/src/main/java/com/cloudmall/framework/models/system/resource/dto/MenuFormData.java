package com.cloudmall.framework.models.system.resource.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 菜单表单数据DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuFormData {
    private Long id;
    private String label;
    private String code;
    private String type;
    private Integer sort;
    private Boolean enable;
    private Long parentId;
    private String path;
    private String component;
    private String icon;
    private Boolean hidden = false;
}
