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
     * 根据SKU IDs批量删除规格关联
     *
     * @param skuIds SKU ID列表
     */
    void removeBySkuIds(List<Long> skuIds);

    /**
     * 统计指定规格值被引用的次数
     * 用于判断规格值是否还被其他SKU引用
     *
     * @param specValueId 规格值ID
     * @return 引用次数
     */
    long countBySpecValueId(Long specValueId);
}