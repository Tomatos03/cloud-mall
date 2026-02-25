package com.onlineshop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlineshop.framework.models.category.vo.CategoryNodeVO;
import com.onlineshop.framework.models.category.ICategoryService;
import com.onlineshop.framework.models.goods.application.GoodsDTO;
import com.onlineshop.framework.models.goods.application.IGoodsAppService;
import com.onlineshop.framework.models.goods.application.vo.GoodsDetailVO;
import com.onlineshop.framework.models.goods.spu.Goods;
import com.onlineshop.framework.models.goods.spu.IGoodsService;
import com.onlineshop.framework.models.goods.unit.IUnitService;
import com.onlineshop.framework.models.goods.unit.Unit;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品管理控制器
 * 合并自 admin/AdminGoodsController + merchant/MerchantGoodsController
 * <p>
 * - 管理员权限：可管理所有商品、直接添加/修改商品
 * - 商家权限：仅能管理自己店铺的商品、需要通过审核流程
 */
@RestController
@RequestMapping("/manage/goods")
@PreAuthorize("hasAuthority('goods:view')")
public class GoodsManageController {
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
    public List<CategoryNodeVO> getCategoryTree() {
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
     * 分页查询商品
     * - 管理员权限：查询所有商品
     * - 商家权限：查询自己店铺的商品
     *
     * @param page     页码，从1开始
     * @param pageSize 每页数量
     * @return 分页结果
     */
    @GetMapping
    public IPage<?> getGoodsPage(
            @RequestParam("page") int page,
            @RequestParam("pageSize") int pageSize
    ) {
        return goodsService.pageQuery(page, pageSize);
    }

    /**
     * 根据ID查询商品详情（管理员权限）
     * 来自 admin/AdminGoodsController
     *
     * @param id 商品ID
     * @return 商品信息
     */
    @GetMapping("/{id}")
    public Goods getGoodsById(@PathVariable Long id) {
        return goodsService.getById(id);
    }

    /**
     * 获取商品详细信息（含全部SKU、规格、描述等）
     *
     * @param id 商品ID
     * @return 商品详细信息
     */
    @GetMapping("/detail/{id}")
    public GoodsDetailVO getGoodsDetail(@PathVariable Long id) {
        return goodsAppService.queryGoodsDetail(id);
    }

    /**
     * 添加商品（管理员权限）
     * 来自 admin/AdminGoodsController
     *
     * @param goods 商品信息
     * @return 是否成功
     */
    @PostMapping
    @PreAuthorize("hasAuthority('goods:add')")
    public boolean addGoods(@RequestBody Goods goods) {
        return goodsService.save(goods);
    }

    /**
     * 修改商品信息（管理员权限）
     * 来自 admin/AdminGoodsController
     *
     * @param goods 商品信息
     * @return 是否成功
     */
    @PutMapping
    @PreAuthorize("hasAuthority('goods:edit')")
    public boolean updateGoods(@RequestBody Goods goods) {
        return goodsService.updateById(goods);
    }

    /**
     * 删除商品
     * 来自 admin/AdminGoodsController + merchant/MerchantGoodsController
     *
     * @param id 商品ID
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('goods:delete')")
    public void deleteGoods(@PathVariable @NotNull Long id) {
        goodsAppService.deleteGoods(id);
    }

    /**
     * 修改商品上下架状态
     * 来自 merchant/MerchantGoodsController
     * 将商品状态设置为上架或下架
     *
     * @param id     商品ID
     * @param status 商品状态（true=上架，false=下架）
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('goods:edit')")
    public void updateGoodsShelfStatus(
            @PathVariable @NotNull Long id,
            @RequestParam @NotNull Boolean status
    ) {
        goodsService.updateGoodsStatus(id, status);
    }
}