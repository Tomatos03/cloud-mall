package com.onlineshop.framework.models.goods.application.vo;

import com.onlineshop.framework.models.goods.sku.SkuDTO;
import com.onlineshop.framework.models.goods.spec.dto.SpecificationsDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审核商品信息VO
 * 用于展示待审核、已拒绝或已撤销的商品审核信息
 *
 * @author Tomatos
 * @date 2026/2/12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditGoodsVO {
    /**
     * 审核ID
     */
    private Long auditId;

    /**
     * 审核状态（0-待审核、1-已通过、2-已拒绝、3-已撤销）
     */
    private String auditStatus;

    /**
     * 审核状态名称（待审核、已通过、已拒绝、已撤销）
     */
    private String auditStatusName;

    /**
     * 审核拒绝原因
     */
    private String auditReason;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 申请时间
     */
    private LocalDateTime createTime;

    /**
     * 待审核的商品信息（变更待审核时显示）
     */
    private PendingGoodsInfo pendingGoodsInfo;

    /**
     * 待审核商品信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingGoodsInfo {
        /**
         * 商品展示图片URL列表
         */
        private List<String> displayImageUrls;
        private List<String> descriptionImageUrls;

        /**
         * 商品名称
         */
        private String goodsName;

        /**
         * 商品卖点
         */
        private String sellPoint;

        /**
         * 商品规格列表
         */
        private List<SpecificationsDTO> specifications;

        /**
         * 商品SKU列表
         */
        private List<SkuDTO> skus;
    }
}

