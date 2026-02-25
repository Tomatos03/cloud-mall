package com.onlineshop.framework.models.system.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/2/11
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserListItemVO {
    private Long id;
    private String username;
    private List<Long> roleIds;
}
