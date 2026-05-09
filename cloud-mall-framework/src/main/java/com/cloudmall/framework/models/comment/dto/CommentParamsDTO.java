package com.cloudmall.framework.models.comment.dto;

import com.cloudmall.framework.common.entity.PageParamsDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 *
 *
 * @author : Tomatos
 * @date : 2026/1/25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentParamsDTO extends PageParamsDTO {
    /**
     * 商品ID
     */
    private Long goodsId;
}
