package com.cloudmall.framework.models.system.resource.vo;

import com.cloudmall.framework.models.system.resource.ResourceMeta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 菜单树视图对象
 * 用于返回给前端的菜单树结构
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuNodeVO {
    private Long id;
    private String type;
    private String code;
    private String description;
    private Integer sort = 0;
    private Boolean enable;
    private ResourceMeta meta;
    private List<MenuNodeVO> children;
}