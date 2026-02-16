package com.onlineshop.framework.models.store;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/2/11
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StoreInfoDTO {
    private String uid;
    private String username;
    private String nickname;
    private String avatarUrl;
    private Long storeId;
    private String storeName;
}
