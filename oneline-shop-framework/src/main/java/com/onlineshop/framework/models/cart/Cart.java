package com.onlineshop.framework.models.cart;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("cart")
public class Cart {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long goodsId;
    private Long skuId;
    private Long num;
    private Long userId;
    private Integer unitId;
}