package com.cloudmall.controller;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cloudmall.framework.models.goods.spu.vo.SpuVO;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.framework.models.category.ICategoryService;
import com.cloudmall.framework.models.category.vo.CategoryNodeVO;
import com.cloudmall.framework.models.goods.application.IGoodsAppService;
import com.cloudmall.framework.models.goods.application.vo.GoodsDetailVO;
import com.cloudmall.framework.models.goods.spu.Goods;
import com.cloudmall.framework.models.goods.spu.IGoodsService;
import com.cloudmall.framework.models.goods.spu.dto.GoodsPageParamsDTO;
import com.cloudmall.framework.models.goods.unit.IUnitService;
import com.cloudmall.framework.models.goods.unit.Unit;
import com.cloudmall.framework.context.AuthUserContext;

/**
 * 商品管理控制器
 * 合并自 admin/AdminGoodsController + merchant/MerchantGoodsController
 * <p>
 * - 管理员权限：可管理所有商品、直接添加/修改商品
 * - 商家权限：仅能管理自己店铺的商品、需要通过审核流程
 */
@RestController
@RequestMapping("/manager/goods")
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
     *
     * @return 分页结果
     */
    @GetMapping
    public IPage<SpuVO> pageGoods(GoodsPageParamsDTO queryDTO) {
        queryDTO.setStoreId(AuthUserContext.getStoreId());
        return goodsService.pageGoods(queryDTO);
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
