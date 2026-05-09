package com.cloudmall.framework.models.seckill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 秒杀活动表（平台创建，无需审核）
 * <p>
 * 活动由管理员直接创建，商家可以选择此活动申请将自己的商品加入
 * 秒杀活动按小时和日期唯一，如"2026-03-06 10:00-11:00"场
 */
@Data
@TableName("seckill_activity")
public class SeckillActivity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 活动名称
     * 例如："618大促"、"周末狂欢"
     */
    private String name;

    /**
     * 开始小时（0-23）
     * 活动固定时长为1小时，如10表示10:00-11:00
     */
    private Integer startHour;

    /**
     * 活动日期
     * 格式：yyyy-MM-dd，用于与startHour组合唯一标识活动
     */
    private String activityDate;

    /**
     * 活动状态
     * 0=报名中（商家可申请）
     * 1=进行中（活动正在进行）
     * 2=已结束（活动已结束）
     * <p>
     * 注意：使用Integer类型而不是枚举，以便数据库直接存储和查询
     * 在需要比较时，使用 SeckillActivityStatus.REGISTRATION.getCode() 等方式获取对应的int值
     */
    private Integer status;

    /**
     * 活动最大商品数
     * 限制该活动中最多可包含的秒杀商品数量
     */
    private Integer maxItems;

    /**
     * 创建时间
     * 由MyMetaObjectHandler自动填充
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     * 由MyMetaObjectHandler自动填充
     */
    private LocalDateTime updateTime;
}