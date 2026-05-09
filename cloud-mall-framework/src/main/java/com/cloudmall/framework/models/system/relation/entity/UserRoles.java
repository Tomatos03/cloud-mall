package com.cloudmall.framework.models.system.relation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户角色关联实体
 * 对应表：user_roles
 *
 * @author Tomatos
 * @date 2026/2/3
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_roles")
public class UserRoles {
    /** 用户ID */
    @TableId(type = IdType.AUTO)
    private Long userId;
    /** 角色ID */
    private Long roleId;
    /** 创建时间 */
    private LocalDateTime createTime;
}