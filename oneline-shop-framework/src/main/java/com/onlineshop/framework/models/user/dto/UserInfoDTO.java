package com.onlineshop.framework.models.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 *
 * @author : Tomatos
 * @date : 2025/12/27
 */
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class UserInfoDTO {
    private String nickname;
    private String avatarUrl;
}