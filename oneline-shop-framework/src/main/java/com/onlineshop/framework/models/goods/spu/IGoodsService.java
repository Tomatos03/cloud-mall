package com.onlineshop.framework.models.goods.spu;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.onlineshop.framework.models.goods.spu.vo.GoodsVO;
import com.onlineshop.framework.models.goods.spu.vo.SpuVO;

import java.util.List;

public interface IGoodsService extends IService<Goods> {
    boolean addGoods(Goods goods);

    boolean updateGoods(Goods goods);

    List<GoodsVO> listByCategoryId(Long categoryId, int limit);

    // ========== 管理员方法 ==========

    /**
     * 分页查询所有商品（管理员权限）
     *
     * @param page 页码，从1开始
     * @param size 每页数量
     * @return 分页结果
     */
    IPage<SpuVO> getGoodsPageAdmin(int page, int size);

    /**
     * 查询指定店铺的商品（管理员权限）
     *
     * @param storeId 店铺ID
     * @param page    页码，从1开始
     * @param size    每页数量
     * @return 分页结果
     */
    IPage<Goods> getStoreGoodsPageAdmin(Long storeId, int page, int size);

    // ========== 商家方法 ==========

    /**
     * 分页查询自己店铺的商品（商家权限）
     *
     * @param page 页码，从1开始
     * @param size 每页数量
     * @return 分页结果
     */
    IPage<SpuVO> getGoodsPageMerchant(int page, int size);

    // ========== 上下架方法 ==========

    /**
     * 更新商品上下架状态
     *
     * @param goodsId 商品ID
     * @param status  商品状态 (true=上架, false=下架)
     */
    void updateGoodsShelfStatus(Long goodsId, Boolean status);

    /**
     * 增加商品销量
     *
     * @param goodsId  商品ID
     * @param quantity 增加数量
     */
    void increaseSales(Long goodsId, Integer quantity);
}
