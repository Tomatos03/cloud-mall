package com.onlineshop.framework.models.store.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 店铺基本信息 VO
 */
@Data
@Builder
public class StoreVO implements Serializable {
    /**
     * 店铺唯一标识 ID
     */
    private String id;

    /**
     * 店铺名称
     */
    private String name;

    /**
     * 店铺简介/描述
     */
    private String description;

    /**
     * 店铺头像 URL
     */
    private String avatarUrl;

    /**
     * 店铺顶部横幅背景图 URL（可选）
     */
    private String banner;
}