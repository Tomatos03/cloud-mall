package com.onlineshop.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlineshop.framework.models.goods.spu.Goods;
import com.onlineshop.framework.models.goods.spu.IGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员商品管理 Controller
 * 管理员拥有最高权限，可以查看和管理所有商品
 */
@RestController
@RequestMapping("/manager/admin/goods")
public class AdminGoodsController {
    @Autowired
    private IGoodsService goodsService;

    /**
     * 查询所有商品（管理员权限）
     * 
     * @return 所有商品列表
     */
    @GetMapping
    public List<Goods> getAllGoods() {
        return goodsService.list();
    }

    /**
     * 根据ID查询商品详情（管理员权限）
     * 
     * @param id 商品ID
     * @return 商品信息
     */
    @GetMapping("/{id}")
    public Goods getGoodsById(@PathVariable Long id) {
        return goodsService.getById(id);
    }

    /**
     * 添加商品（管理员权限）
     * 
     * @param goods 商品信息
     * @return 是否成功
     */
    @PostMapping
    public boolean addGoods(@RequestBody Goods goods) {
        return goodsService.addGoods(goods);
    }

    /**
     * 修改商品信息（管理员权限）
     * 
     * @param goods 商品信息
     * @return 是否成功
     */
    @PutMapping
    public boolean updateGoods(@RequestBody Goods goods) {
        return goodsService.updateGoods(goods);
    }

    /**
     * 删除商品（管理员权限）
     * 
     * @param id 商品ID
     * @return 是否成功
     */
    @DeleteMapping("/{id}")
    public boolean deleteGoods(@PathVariable Long id) {
        return goodsService.removeById(id);
    }

    /**
     * 分页查询所有商品（管理员权限）
     * 
     * @param page 页码，从1开始
     * @param size 每页数量
     * @return 分页结果
     */
    @GetMapping("/page")
    public IPage<Goods> getGoodsPage(@RequestParam("page") int page,
                                     @RequestParam("pageSize") int size) {
        return goodsService.getGoodsPageAdmin(page, size);
    }

    /**
     * 查询指定店铺的商品（管理员权限）
     * 
     * @param storeId 店铺ID
     * @param page 页码，从1开始
     * @param size 每页数量
     * @return 分页结果
     */
    @GetMapping("/store/{storeId}/page")
    public IPage<Goods> getStoreGoodsPage(@PathVariable Long storeId,
                                          @RequestParam("page") int page,
                                          @RequestParam("pageSize") int size) {
        return goodsService.getStoreGoodsPageAdmin(storeId, page, size);
    }
}