package com.cloudmall.framework.models.system.user.dto;

import lombok.Data;
import java.util.List;

/**
 * 用户更新请求 DTO
 *
 * @author : Tomatos
 * @date : 2026/3/3
 */
@Data
public class UserUpdateDTO {
    private Long id;
    private String username;
    private String password;
    private List<Long> roleIds;
}
