package com.cloudmall.framework.models.store.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 店铺更新 DTO
 * 仅包含可更新的字段
 *
 * @author Tomatos
 * @date 2025/12/25
 */
@Data
public class StoreUpdateDTO implements Serializable {
    /**
     * 店铺名称
     */
    private String name;

    /**
     * 店铺简介/描述
     */
    private String info;

    /**
     * 店铺头像 URL
     */
    private String avatarUrl;

    /**
     * 店铺顶部横幅背景图 URL
     */
    private String banner;
}