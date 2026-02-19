package com.onlineshop.framework.models.chat.dto;

import com.onlineshop.framework.common.entity.PageParamsDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

/**
 * 消息历史分页查询参数DTO
 * 继承分页参数，补充会话ID
 *
 * @author : Tomatos
 * @date : 2026/02/02
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IMPageParamsDTO extends PageParamsDTO {
    /**
     * 会话ID
     */
    @NotNull(message = "会话ID不能为空")
    private Long sessionId;
}