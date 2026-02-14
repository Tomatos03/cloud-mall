package com.onlineshop.framework.models.system.resource;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "resources", autoResultMap = true)
public class Resource {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String type;

    private String code;

    @TableField(typeHandler = JacksonTypeHandler .class)
    private ResourceMeta meta;

    private Long parentId;

    private Integer sort;

    private String description;
    private Boolean enable;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}