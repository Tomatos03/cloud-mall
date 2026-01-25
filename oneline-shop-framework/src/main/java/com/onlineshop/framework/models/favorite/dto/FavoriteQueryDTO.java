package com.onlineshop.framework.models.favorite.dto;

import com.onlineshop.framework.common.entity.PageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 收藏分页查询条件DTO
 *
 * @author : Tomatos
 * @date : 2025/12/24
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FavoriteQueryDTO extends PageQueryDTO {
}
