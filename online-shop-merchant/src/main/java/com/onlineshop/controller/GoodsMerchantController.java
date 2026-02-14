package com.onlineshop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlineshop.controller.dto.GoodsRepublishDTO;
import com.onlineshop.controller.dto.GoodsStatusUpdateDTO;
import com.onlineshop.framework.models.audit.application.IAuditAppService;
import com.onlineshop.framework.models.audit.enums.AuditType;
import com.onlineshop.framework.models.goods.application.GoodsDTO;
import com.onlineshop.framework.models.goods.application.IGoodsAppService;
import com.onlineshop.framework.models.goods.application.vo.GoodsDetailWithAuditVO;
import com.onlineshop.framework.models.goods.spu.IGoodsService;
import com.onlineshop.framework.models.goods.spu.vo.SpuVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 商家商品管理 Controller
 * 商家只能管理自己店铺的商品
 *
 * @author : Tomatos
 * @date : 2026/2/11
 */
@RequestMapping("/merchant/goods")
@RestController
@RequiredArgsConstructor
public class GoodsMerchantController {
    private final IGoodsAppService goodsAppService;
    private final IGoodsService goodsService;
    private final IAuditAppService auditAppService;

    /**
     * 获取商品详情（包含审核信息）
     * GET /merchant/goods/detail-with-audit/{goodsId}
     *
     * @param goodsId 商品ID
     * @return 商品详情及其关联的审核信息
     */
    @GetMapping("/detail/{goodsId}")
    public GoodsDetailWithAuditVO getGoodsDetailWithAudit(@PathVariable Long goodsId) {
        return goodsAppService.getGoodsDetailWithAudit(goodsId);
    }

    /**
     * 更新商品状态（上下架）
     * PUT /merchant/goods/{goodsId}/status
     *
     * @param goodsId 商品ID
     * @param statusDTO 包含status字段的状态更新DTO
     */
    @PutMapping("/{goodsId}/status")
    public void updateGoodsStatus(
            @PathVariable @NotNull Long goodsId,
            @Valid @RequestBody GoodsStatusUpdateDTO statusDTO
    ) {
        goodsService.updateGoodsStatus(goodsId, statusDTO.getStatus());
    }

    /**
     * 提交商品（新增或更新商品，需要审核）
     * POST /merchant/goods
     *
     * @param payload 商品提交请求对象
     *               - 新商品：goodsId 为 null
     *               - 更新商品：goodsId 不为 null
     */
    @PostMapping
    public void submitGoods(@Valid @RequestBody GoodsDTO payload) {
        auditAppService.submitAudit(AuditType.GOODS, payload);
    }

    /**
     * 重新发布商品（审核被撤销或拒绝后重新提交）
     * POST /merchant/goods/republish
     *
     * @param republishDTO 包含 auditId 和商品信息的重新发布请求对象
     */
    @PostMapping("/republish")
    public void republishGoods(@Valid @RequestBody GoodsRepublishDTO republishDTO) {
        Long auditId = republishDTO.getAuditId();
        goodsAppService.republishGoodsFromAudit(auditId, republishDTO);
    }

    /**
     * 分页查询商品列表
     * GET /merchant/goods
     *
     * @param page 页码，从1开始
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
