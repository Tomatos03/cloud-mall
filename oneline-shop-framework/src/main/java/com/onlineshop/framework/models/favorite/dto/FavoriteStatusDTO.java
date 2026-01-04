package com.onlineshop.framework.models.favorite.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 *
 *
 * @author : Tomatos
 * @date : 2025/12/24
 */
@Data
public class FavoriteStatusDTO {
    @JsonProperty("isFavorite")
    private boolean isFavorite;
    private Long favoriteId;
}
