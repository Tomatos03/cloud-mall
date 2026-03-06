package com.onlineshop.framework.models.seckill.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 秒杀活动数据传输对象
 * 
 * 用于管理员创建、查询秒杀活动时的数据传输
 */
@Data
public class SeckillActivityDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 活动名称
     */
    private String name;

    /**
     * 开始小时（0-23）
     */
    private Integer startHour;

    /**
     * 活动日期
     * 格式：yyyy-MM-dd
     */
    private String activityDate;

    /**
     * 活动最大商品数
     */
    private Integer maxItems;
}
