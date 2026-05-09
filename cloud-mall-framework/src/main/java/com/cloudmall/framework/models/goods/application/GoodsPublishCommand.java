package com.cloudmall.framework.models.goods.application;

import com.cloudmall.framework.models.goods.sku.SkuDTO;
import com.cloudmall.framework.models.goods.spec.dto.SpecificationsDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 商品发布命令对象
 *
 * @author Tomatos
 * @date 2026/2/26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsPublishCommand {
    /**
     * 商品ID
     * <p>
     * 可为空：
     * - 新增商品时为 null
     * - 编辑现有商品时有值
     */
    private Long goodsId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 单位ID
     */
    private Long unitId;

    /**
     * 单位名称
     */
    private String unitName;

    /**
     * 商品卖点/描述
     * <p>
     * 营销文案，用于商品列表展示
     */
    private String sellPoint;

    /**
     * 展示图URL列表
     * <p>
     * 数量范围：1-5张
     * 这些图片用于商品列表和详情页面展示
     */
    private List<String> displayImageUrls;

    /**
     * 描述图URL列表
     * <p>
     * 数量范围：1-8张
     * 这些图片用于商品详情页面的详细描述
     */
    private List<String> descriptionImageUrls;

    /**
     * 店铺ID
     */
    private Long storeId;

    /**
     * 店铺名称
     */
    private String storeName;

    /**
     * 商品状态
     * <p>
     * true = 上架 (1)
     * false = 下架 (0)
     */
    private Boolean status;

    /**
     * 规格列表
     * <p>
     * 数量范围：1-3个
     * 例如：[{"name": "颜色", "values": ["红", "蓝"]}, {"name": "尺码", "values": ["M", "L"]}]
     */
    private List<SpecificationsDTO> specifications;

    /**
     * SKU列表
     * <p>
     * 库存单位，包含规格、价格、库存等信息
     */
    private List<SkuDTO> skus;
}
