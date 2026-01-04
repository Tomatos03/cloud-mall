package com.onlineshop.framework.models.user.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 *
 * @author : Tomatos
 * @date : 2025/12/31
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoVO {
    private String uid; // 用户id
    private String nickname;
    private String avatarUrl;
    private String phone;
    private String email;
    private String bio; // 个人简介
}