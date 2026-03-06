package com.onlineshop.framework.models.audit.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 秒杀商品审核详情VO
 * 用于管理后台展示审核详情
 * 
 * 支持单商品和批量商品的统一展示
 *
 * @author Tomatos
 * @date 2026/3/6
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillAuditDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 审核ID
     */
    private Long auditId;

    /**
     * 申请人
     */
    private String applicantName;

    /**
     * 申请时间
     */
    private LocalDateTime createTime;

    /**
     * 审核状态：PENDING/APPROVED/REJECTED/REVOKED
     */
    private String status;

    /**
     * 秒杀活动ID
     */
    private Long activityId;

    /**
     * 秒杀活动名称
     */
    private String activityName;

    /**
     * 商品列表
     * 支持单个或多个商品
     */
    private List<SeckillGoodsItemVO> items;
}
