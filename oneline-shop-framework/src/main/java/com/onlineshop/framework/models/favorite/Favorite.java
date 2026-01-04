package com.onlineshop.framework.models.favorite;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("favorite")
public class Favorite {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long goodsId;
    private Date addedAt;
    private String goodsTitle;
    private String goodsImg;
    private Long goodsPrice;
    private String goodsDesc;
    private Long storeId;
}
