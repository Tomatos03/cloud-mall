package com.onlineshop.framework.models.search.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlineshop.framework.models.goods.spu.dto.GoodsSearchDTO;
import com.onlineshop.framework.models.goods.spu.vo.GoodsCardVO;
import com.onlineshop.framework.models.search.index.GoodsIndex;

/**
 * 搜索服务接口
 * 提供商品搜索功能，包括ES索引管理和搜索查询
 *
 * @author : Tomatos
 * @date : 2025/1/1
 */
public interface IGoodsEsService {

    /**
     * 保存商品索引到ES
     *
     * @param goodsIndex 商品索引对象
     */
    void saveGoodsIndex(GoodsIndex goodsIndex);

    /**
     * 批量保存商品索引到ES
     *
     * @param goodsIndexList 商品索引列表
     */
    void saveGoodsIndexBatch(Iterable<GoodsIndex> goodsIndexList);

    /**
     * 删除商品索引
     *
     * @param goodsId 商品ID
     */
    void deleteGoodsIndex(Long goodsId);

    /**
     * 批量删除商品索引
     *
     * @param goodsIds 商品ID列表
     */
    void deleteGoodsIndexBatch(Iterable<Long> goodsIds);

    /**
     * 搜索商品
     *
     * @param searchDTO 搜索条件DTO
     * @return 分页的商品卡片VO列表
     */
    IPage<GoodsCardVO> searchGoods(GoodsSearchDTO searchDTO);

    /**
     * 重建所有商品索引
     * 该操作会删除现有索引并重新创建
     */
    void rebuildAllGoodsIndex();
}