package com.cloudmall.framework.models.goods.spu;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import com.cloudmall.framework.models.goods.spu.dto.GoodsPageParamsDTO;
import com.cloudmall.framework.models.goods.spu.vo.SpuVO;

public interface IGoodsService extends IService<Goods> {
    List<Goods> queryEnableGoodsList();

    List<Goods> queryGoodsListByIds(Collection<? extends Serializable> ids);

    /**
     * 分页查询商品列表
     *
     * @param queryDTO 查询参数：page/pageSize/status/storeId
     * @return 分页结果
     */
    IPage<SpuVO> pageGoods(GoodsPageParamsDTO queryDTO);

    /**
     * 更新商品上下架状态
     *
     * @param goodsId 商品ID
     * @param status  商品状态 (true=上架, false=下架)
     */
    void updateGoodsStatus(Long goodsId, Boolean status);

    /**
     * 增加商品销量
     *
     * @param goodsId  商品ID
     * @param quantity 增加数量
     */
    void increaseSales(Long goodsId, Integer quantity);

    /**
     * 批量查询多个分类下的商品（包括子分类）
     * 相比逐个查询每个分类，这个方法只执行一次数据库查询，性能更优
     *
     * @param categoryIds 分类ID列表（包括子分类的ID）
     * @param limit       返回的商品最大数量
     * @return 按销量降序排列的商品列表
     */
    List<Goods> queryGoodsByMultipleCategoryIds(List<Long> categoryIds, int limit);
}
