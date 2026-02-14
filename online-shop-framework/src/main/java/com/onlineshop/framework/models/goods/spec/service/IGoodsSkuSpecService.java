package com.onlineshop.framework.models.goods.spec.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.onlineshop.framework.models.goods.spec.entity.GoodsSkuSpec;

import java.util.List;

public interface IGoodsSkuSpecService extends IService<GoodsSkuSpec> {

    /**
     * 根据SKU ID查询规格信息
     *
     * @param skuId SKU ID
     * @return 规格列表
     */
    List<GoodsSkuSpec> listBySkuId(Long skuId);

    /**
     * 根据规格值ID查询SKU规格关联
     *
     * @param specValueId 规格值ID
     * @return SKU规格关联列表
     */
    List<GoodsSkuSpec> listBySpecValueId(Long specValueId);

    /**
     * 为SKU添加规格
     *
     * @param skuId       SKU ID
     * @param specId      规格ID
     * @param specValueId 规格值ID
     * @return 是否成功
     */
    boolean addSpecToSku(Long skuId, Long specId, Long specValueId);

    /**
     * 批量为SKU添加规格
     *
     * @param specList 规格列表
     * @return 是否成功
     */
    boolean batchAddSpecToSku(List<GoodsSkuSpec> specList);

    /**
     * 删除SKU的规格关联
     *
     * @param skuId SKU ID
     * @return 删除数量
     */
    int removeBySkuId(Long skuId);

    /**
     * 删除指定的规格关联
     *
     * @param skuId  SKU ID
     * @param specId 规格ID
     * @return 删除数量
     */
    int removeBySkuIdAndSpecId(Long skuId, Long specId);

    /**
     * 根据SKU IDs批量删除规格关联
     *
     * @param skuIds SKU ID列表
     * @return 删除数量
     */
    int removeBySkuIds(List<Long> skuIds);

    /**
     * 统计指定规格值被引用的次数
     * 用于判断规格值是否还被其他SKU引用
     *
     * @param specValueId 规格值ID
     * @return 引用次数
     */
    long countBySpecValueId(Long specValueId);
}