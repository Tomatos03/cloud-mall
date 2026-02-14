package com.onlineshop.framework.models.notice;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("notice")
@Data
public class Notice {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String content;
}