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
     * 获取SKU详情
     *
     * @param skuId SKU ID
     * @return SKU信息
     */
    GoodsSku getSkuDetail(Long skuId);

    /**
     * 添加SKU
     *
     * @param goodsSku SKU信息
     * @return 是否成功
     */
    boolean addSku(GoodsSku goodsSku);

    /**
     * 修改SKU
     *
     * @param goodsSku SKU信息
     * @return 是否成功
     */
    boolean updateSku(GoodsSku goodsSku);

    /**
     * 删除SKU
     *
     * @param skuId SKU ID
     * @return 是否成功
     */
    boolean removeSku(Long skuId);

    /**
     * 根据商品ID删除所有SKU
     *
     * @param goodsId 商品ID
     * @return 删除数量
     */
    int removeByGoodsId(Long goodsId);

    /**
     * 扣减库存
     *
     * @param skuId   SKU ID
     * @param quantity 扣减数量
     * @return 是否成功
     */
    boolean deductInventory(Long skuId, Long quantity);

    /**
     * 增加销量
     *
     * @param skuId   SKU ID
     * @param quantity 增加数量
     * @return 是否成功
     */
    boolean increaseSales(Long skuId, Long quantity);
}