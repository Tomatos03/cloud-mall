package com.onlineshop.framework.models.favorite.dto;

import com.onlineshop.framework.common.entity.PageParamsDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 收藏分页查询条件DTO
 *
 * @author : Tomatos
 * @date : 2025/12/24
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FavoriteParamsDTO extends PageParamsDTO {
}
