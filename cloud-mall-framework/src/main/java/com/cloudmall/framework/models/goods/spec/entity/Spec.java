package com.cloudmall.framework.models.goods.spec.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("spec")
public class Spec {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 规格名，如 颜色、尺码
     */
    private String name;
    
    /**
     * 排序
     */
    private Integer sort;
    
    /**
     * 状态 1启用 0禁用
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
}