package com.cloudmall.framework.models.store.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/1/20
 */
@Builder
@AllArgsConstructor
@NotBlank
@Data
public class StoreInfoVO implements Serializable {
    private Long storeId;
    private String storeName;
    private String storeAvatarUrl;
}
