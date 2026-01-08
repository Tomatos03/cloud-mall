package com.onlineshop.framework.models.unit;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 单位实体类
 *
 * @author Tomatos
 * @date 2026/01/05
 */
@Data
@TableName("goods_unit")
public class Unit {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer status;
    private Integer sort;
}
