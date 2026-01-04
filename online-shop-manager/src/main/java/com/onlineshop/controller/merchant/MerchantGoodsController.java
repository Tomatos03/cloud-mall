package com.onlineshop.controller.merchant;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlineshop.framework.models.goods.Goods;
import com.onlineshop.framework.models.goods.IGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 商家商品管理 Controller
 * 商家只能管理自己店铺的商品
 */
@RestController
@RequestMapping("/manager/merchant/goods")
public class MerchantGoodsController {
    @Autowired
    private IGoodsService goodsService;

    /**
     * 查询自己店铺的商品
     * 
     * @return 商品列表
     */
    @GetMapping
    public IPage<Goods> getMyGoods(@RequestParam("page") int page,
                                   @RequestParam("pageSize") int size) {
        return goodsService.getGoodsPageMerchant(page, size);
    }

    /**
     * 根据ID查询商品详情
     * 需验证商品是否属于当前商家
     * 
     * @param id 商品ID
     * @return 商品信息
     */
    @GetMapping("/{id}")
    public Goods getGoodsById(@PathVariable Long id) {
        return goodsService.getGoodsDetailMerchant(id);
    }

    /**
     * 添加商品
     * 商家可以添加商品到自己的店铺
     * 
     * @param goods 商品信息
     * @return 是否成功
     */
    @PostMapping
    public boolean addGoods(@RequestBody Goods goods) {
        return goodsService.addGoodsMerchant(goods);
    }

    /**
     * 修改商品信息
     * 需验证商品是否属于当前商家
     * 
     * @param goods 商品信息
     * @return 是否成功
     */
    @PutMapping
    public boolean updateGoods(@RequestBody Goods goods) {
        return goodsService.updateGoodsMerchant(goods);
    }

    /**
     * 删除商品
     * 需验证商品是否属于当前商家
     * 
     * @param id 商品ID
     * @return 是否成功
     */
    @DeleteMapping("/{id}")
    public boolean deleteGoods(@PathVariable Long id) {
        return goodsService.removeGoodsMerchant(id);
    }

    /**
     * 分页查询自己店铺的商品
     * 
     * @param page 页码，从1开始
     * @param size 每页数量
     * @return 分页结果
     */
    @GetMapping("/page")
    public IPage<Goods> getGoodsPage(@RequestParam("page") int page,
                                     @RequestParam("pageSize") int size) {
        return goodsService.getGoodsPageMerchant(page, size);
    }
}