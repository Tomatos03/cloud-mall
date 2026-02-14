package com.onlineshop.framework.models.message;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/1/25
 */
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@TableName("message_log")
public class MessageLog {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String bizId;

    private String bizType;

    private String topic;

    private String payload;

    @Builder.Default
    private Integer status = 0;

    @Builder.Default
    private Integer retryCount = 0;

    private LocalDateTime nextRetryTime;

    private String errorMsg;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
