package com.cloudmall.framework.models.seckill.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 秒杀活动视图对象
 * 
 * 用于返回秒杀活动的详细信息到前端
 */
@Data
public class SeckillActivityVO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 主键
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
     * 活动状态
     * 0=报名中, 1=进行中, 2=已结束
     */
    private Integer status;

    /**
     * 活动最大商品数
     */
    private Integer maxItems;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
