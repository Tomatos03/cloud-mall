package com.onlineshop.controller;

import java.util.Collections;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.onlineshop.framework.models.goods.application.GoodsStatusUpdateDTO;
import com.onlineshop.framework.models.audit.application.impl.GoodsAuditor;
import com.onlineshop.framework.models.audit.dto.AuditSubmitDTO;
import com.onlineshop.framework.models.audit.dto.GoodsAuditItemDTO;
import com.onlineshop.framework.models.audit.enums.AuditBizType;
import com.onlineshop.framework.models.goods.application.IGoodsAppService;
import com.onlineshop.framework.models.goods.application.vo.GoodsDetailVO;
import com.onlineshop.framework.models.goods.sku.IGoodsSkuService;
import com.onlineshop.framework.models.goods.sku.MerchantGoodsSkuItemDTO;
import com.onlineshop.framework.models.goods.sku.MerchantGoodsSkuParamsDTO;
import com.onlineshop.framework.models.goods.spu.IGoodsService;
import com.onlineshop.framework.models.goods.spu.vo.SpuVO;

/**
 * 商家商品管理 Controller
 * 商家只能管理自己店铺的商品
 *
 * @author : Tomatos
 * @date : 2026/2/11
 */
@RequestMapping("/goods")
@RestController
@RequiredArgsConstructor
public class GoodsMerchantController {
    private final IGoodsAppService goodsAppService;
    private final IGoodsService goodsService;
    private final IGoodsSkuService goodsSkuService;
    private final GoodsAuditor goodsAuditor;

    /**
     * 获取商品详情（包含审核信息）
     * GET /merchant/goods/detail-with-audit/{goodsId}
     *
     * @param goodsId 商品ID
     * @return 商品详情及其关联的审核信息
     */
    @GetMapping("/detail/{goodsId}")
    public GoodsDetailVO getGoodsDetailWithAudit(@PathVariable Long goodsId) {
        return goodsAppService.queryGoodsDetail(goodsId);
    }

    /**
     * 更新商品状态（上下架）
     * PUT /merchant/goods/{goodsId}/status
     *
     * @param goodsId   商品ID
     * @param statusDTO 包含status字段的状态更新DTO
     */
    @PutMapping("/{goodsId}/status")
    public void updateGoodsStatus(
            @PathVariable @NotNull Long goodsId,
            @Valid @RequestBody GoodsStatusUpdateDTO statusDTO
    ) {
        goodsService.updateGoodsStatus(goodsId, statusDTO.getStatus());
    }

    @PostMapping("/publish")
    public void submitGoods(@Valid @RequestBody GoodsAuditItemDTO goodsAuditItemDTO) {
        AuditSubmitDTO<GoodsAuditItemDTO> submitDTO = AuditSubmitDTO.of(
                AuditBizType.GOODS.getCode(),
                Collections.singletonList(goodsAuditItemDTO)
        );
        goodsAuditor.submitAudit(submitDTO);
    }

    /**
     * 分页查询商品列表
     * GET /merchant/goods
     *
     * @param page     页码，从1开始
     * @param pageSize 每页数量
     * @return 分页商品列表
     */
    @GetMapping
    public IPage<SpuVO> pageGoods(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize
    ) {
        return goodsService.pageQuery(page, pageSize);
    }

    /**
     * 分页查询SKU列表（当前商家）
     * GET /merchant/goods/skus
     *
     * @param params 分页参数
     * @return SKU分页列表
     */
    @GetMapping("/skus")
    public IPage<MerchantGoodsSkuItemDTO> pageSkus(MerchantGoodsSkuParamsDTO params) {
        return goodsSkuService.pageMerchantGoodsSkus(params);
    }

    /**
     * 删除商品
     * DELETE /merchant/goods/{goodsId}
     *
     * @param goodsId 商品ID
     */
    @DeleteMapping("/{goodsId}")
    public void deleteGoods(@PathVariable @NotNull Long goodsId) {
        goodsAppService.deleteGoods(goodsId);
    }
}
