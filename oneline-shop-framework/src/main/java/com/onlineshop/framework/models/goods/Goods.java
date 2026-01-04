package com.onlineshop.framework.models.goods;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("goods")
public class Goods {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer categoryId; // 分类Id
    private String info;
    private String description;
    private String img;
    private String imgList;
    private Long inventory;
    private Long price;
    private String unit;
    private Long storeId;
    private String storeName;
    private Long sales;
    private Date date;
    private Boolean status;
}