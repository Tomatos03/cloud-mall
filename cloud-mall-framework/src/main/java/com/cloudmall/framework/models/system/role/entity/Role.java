package com.cloudmall.framework.models.system.role.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 角色表实体类
 * 对应数据库 roles 表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("roles")
public class Role {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    private Boolean enable;

    private LocalDateTime createTime;
}