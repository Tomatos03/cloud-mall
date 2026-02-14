package com.onlineshop.framework.models.category;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 商品分类实体类
 *
 * @author Tomatos
 * @date 2025/12/20
 */
@Data
@TableName("category")
public class Category {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Long parentId;
    private Integer level;
    private Integer sort;
    private Boolean status;
}
