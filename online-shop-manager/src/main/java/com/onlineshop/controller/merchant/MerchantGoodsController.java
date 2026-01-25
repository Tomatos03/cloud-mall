package com.onlineshop.controller.merchant;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlineshop.framework.models.category.CategoryVO;
import com.onlineshop.framework.models.category.ICategoryService;
import com.onlineshop.framework.models.goods.application.GoodsDTO;
import com.onlineshop.framework.models.goods.application.vo.GoodsDetailVO;
import com.onlineshop.framework.models.goods.application.IGoodsAppService;
import com.onlineshop.framework.models.goods.spu.IGoodsService;
import com.onlineshop.framework.models.goods.spu.vo.SpuVO;
import com.onlineshop.framework.models.unit.IUnitService;
import com.onlineshop.framework.models.unit.Unit;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商家商品管理 Controller
 * 商家只能管理自己店铺的商品
 */
@RestController
@RequestMapping("/manager/merchant/goods")
public class MerchantGoodsController {
    @Autowired
    private IGoodsAppService goodsAppService;
    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private ICategoryService categoryService;
    @Autowired
    private IUnitService unitService;

    /**
     * 获取分类树（级联）
     *
     * @return 分类树列表
     */
    @GetMapping("/category/tree")
    public List<CategoryVO> getCategoryTree() {
        return categoryService.getCategoryTree();
    }

    /**
     * 获取商品单位列表
     * 返回GoodsUnitVO列表，包含完整的单位信息
     *
     * @return 单位列表
     */
    @GetMapping("/units")
    public List<Unit> getGoodsUnits() {
        return unitService.getAllUnit();
    }

    /**
     * 发布或更新商品（提交审核）
     * 新商品：payload 中不包含 goodsId
     * 更新商品：payload 中包含 goodsId
     *
     * @param payload 商品发布请求对象
     */
    @PostMapping
    public void submitGoodsAudit(
            @Valid
            @RequestBody
            GoodsDTO payload
    ) {
        goodsAppService.submitGoodsAudit(payload);
    }

    /**
     * 分页查询自己店铺的商品
     *
     * @param page 页码，从1开始
     * @param size 每页数量
     * @return 分页结果
     */
    @GetMapping
    public IPage<SpuVO> getGoodsPage(
            @RequestParam("page") int page,
            @RequestParam("pageSize") int size
    ) {
        return goodsService.getGoodsPageMerchant(page, size);
    }

    /**
     * 删除商品
     *
     * @param id 商品ID
     */
    @DeleteMapping("/{id}")
    public void deleteGoods(@PathVariable @NotNull Long id) {
        goodsAppService.deleteGoods(id);
    }

    /**
     * 重新发布处于撤销状态的审核商品
     * 用于商家在审核被撤销后，重新提交商品审核
     *
     * @param auditId 被撤销的审核记录ID
     * @param payload 新的商品发布请求对象
     */
    @PostMapping("/republish/{id}")
    public void republishGoodsFromAudit(
            @PathVariable("id") Long auditId,
            @Valid @RequestBody GoodsDTO payload
    ) {
        goodsAppService.republishGoodsFromAudit(auditId, payload);
    }
    /**
     * 获取商品详细信息（含全部SKU、规格、描述等）
     * @param id 商品ID
     * @return 商品详细信息
     */
    @GetMapping("/detail/{id}")
    public GoodsDetailVO getGoodsDetail(@PathVariable Long id) {
        return goodsAppService.getGoodsDetail(id);
    }

    /**
     * 修改商品上下架状态
     * 将商品状态设置为上架或下架
     *
     * @param id     商品ID
     * @param status 商品状态（true=上架，false=下架）
     */
    @PutMapping("/{id}/status")
    public void updateGoodsShelfStatus(
            @PathVariable @NotNull Long id,
            @RequestParam @NotNull Boolean status
    ) {
        goodsService.updateGoodsShelfStatus(id, status);
    }
}