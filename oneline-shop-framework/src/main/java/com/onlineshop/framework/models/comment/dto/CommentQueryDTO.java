package com.onlineshop.framework.models.comment.dto;

import com.onlineshop.framework.common.entity.PageQueryDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


/**
 *
 *
 * @author : Tomatos
 * @date : 2026/1/25
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CommentQueryDTO extends PageQueryDTO {
    /**
     * 商品ID
     */
    private Long goodsId;
}
