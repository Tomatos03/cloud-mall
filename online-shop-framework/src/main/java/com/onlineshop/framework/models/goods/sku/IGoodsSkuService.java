package com.onlineshop.framework.models.goods.sku;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

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
     * 扣减SKU库存
     *
     * @param skuId    SKU ID
     * @param inventory 当前库存
     * @param quantity 扣减数量
     */
    void deductInventory(Long skuId, Integer quantity);

    /**
     * 增加SKU销量
     *
     * @param skuId    SKU ID
     * @param inventory 当前库存
     * @param quantity 增加数量
     */
    void increaseSales(Long skuId, Integer quantity);

    /**
     * 分页查询当前商家的SKU列表
     *
     * @param params 分页与筛选参数
     * @return SKU分页数据
     */
    IPage<MerchantGoodsSkuItemDTO> pageMerchantGoodsSkus(MerchantGoodsSkuParamsDTO params);
}
