package com.onlineshop.controller.merchant;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlineshop.framework.models.category.CategoryVO;
import com.onlineshop.framework.models.category.ICategoryService;
import com.onlineshop.framework.models.goods.application.IGoodsAppService;
import com.onlineshop.framework.models.goods.spu.Goods;
import com.onlineshop.framework.models.goods.spu.IGoodsService;
import com.onlineshop.framework.models.goods.spu.dto.GoodsPublishPayload;
import com.onlineshop.framework.models.goods.spu.vo.GoodsItemVO;
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
        return unitService.list();
    }

    /**
     * 发布新商品
     *
     * @param payload 商品发布请求对象
     */
    @PostMapping
    public void publishGoods(@Valid @RequestBody GoodsPublishPayload payload) {
        goodsAppService.publishGoods(payload);
    }

    /**
     * 更新商品
     *
     * @param payload 商品发布请求对象（包含id）
     */
    @PutMapping
    public void updateGoods(@Valid @RequestBody GoodsPublishPayload payload) {
        goodsAppService.updateGoods(payload);
    }

    /**
     * 获取商品详情（编辑模式）
     * 返回包含规格和SKU完整信息的商品详情
     *
     * @param id 商品ID
     * @return 商品详情信息
     */
    @GetMapping("/{id}")
    public GoodsItemVO getGoodsItem(@PathVariable Long id) {
        return goodsAppService.getGoodsItem(id);
    }

    /**
     * 分页查询自己店铺的商品
     *
     * @param page 页码，从1开始
     * @param size 每页数量
     * @return 分页结果
     */
    @GetMapping
    public IPage<Goods> getGoodsPage(@RequestParam("page") int page,
                                     @RequestParam("pageSize") int size) {
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
}