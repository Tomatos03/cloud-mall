package com.onlineshop.framework.models.goods.sku;

import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface IGoodsSkuService extends IService<GoodsSku> {

    /**
     * 根据商品ID查询所有SKU
     *
     * @param goodsId 商品ID
     * @return SKU列表
     */
    List<GoodsSku> listByGoodsId(Long goodsId);

    /**
     * 添加SKU
     *
     * @param goodsSku SKU信息
     * @return 是否成功
     */
    boolean addSku(GoodsSku goodsSku);
    /**
     * 根据商品ID删除所有SKU
     *
     * @param goodsId 商品ID
     */
    void removeByGoodsId(Long goodsId);
    /**
     * 扣减库存并增加销量
     * 同时执行库存扣减和销量增加操作
     *
     * @param skuId    SKU ID
     * @param quantity 扣减/增加数量
     */
    void deductInventoryAndIncreaseSales(Long skuId, Integer quantity);
}