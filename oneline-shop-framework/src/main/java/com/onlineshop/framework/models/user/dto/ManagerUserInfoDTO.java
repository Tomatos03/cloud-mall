package com.onlineshop.framework.models.user.dto;

import lombok.Data;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/1/2
 */
@Data
public class ManagerUserInfoDTO {
    private Long uid;
    private String username;
    private String nickname;
    private String role;
    private String phone;
    private String email;
    private String avatarUrl;
    private String storeName;
    private Long storeId;
}
