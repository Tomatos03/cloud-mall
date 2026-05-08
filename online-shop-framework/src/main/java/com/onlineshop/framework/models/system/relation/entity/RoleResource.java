package com.onlineshop.framework.models.system.relation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色资源关联实体
 * 对应表：role_resources
 *
 * @author Tomatos
 * @date 2026/2/3
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("role_resources")
public class RoleResource {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 角色ID（复合主键的一部分） */
    private Long roleId;
    
    /** 资源ID（复合主键的一部分） */
    private Long resourceId;
}