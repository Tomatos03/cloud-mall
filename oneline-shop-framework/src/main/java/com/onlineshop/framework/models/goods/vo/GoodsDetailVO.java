package com.onlineshop.framework.models.goods.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 *
 *
 * @author : Tomatos
 * @date : 2025/12/23
 */
@Data
@AllArgsConstructor
@Builder
public class GoodsDetailVO {
    private Long storeId;
    private String storeName;
    private String storeAvatarUrl;
    private String goodsName;
    private String goodsInfo;
    private Long price;
    private Long sale;
    private Long inventory;
    private String mainImg; // 主图
    private List<String> subImg; // 副图
    private String description;
    private Date createTime;
}