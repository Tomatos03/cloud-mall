package com.onlineshop.framework.models.banner;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

/**
 * 轮播图实体类
 *
 * @author Tomatos
 * @date 2025/12/17
 */
@Data
@TableName("banner")
@Builder
public class Banner {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String imageUrl;
    private Long goodsId;
    private String goodsName;
    private Boolean isRecommend;
}