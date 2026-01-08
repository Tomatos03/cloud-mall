package com.onlineshop.framework.models.goods.spu;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.onlineshop.framework.models.goods.spu.dto.GoodsSearchDTO;
import com.onlineshop.framework.models.goods.spu.vo.GoodsCardVO;
import com.onlineshop.framework.models.goods.spu.vo.GoodsDetailVO;
import com.onlineshop.framework.models.goods.spu.vo.GoodsVO;

import java.util.List;

public interface IGoodsService extends IService<Goods> {

    boolean addGoods(Goods goods);

    /**
     * 分页查询商品
     *
     * @param page 页码，从1开始
     * @param size 每页数量
     * @return 分页结果
     */
    IPage<Goods> getGoodsPage(int page, int size);

    boolean updateGoods(Goods goods);

    List<Goods> queryByIds(List<Long> goodsIds);

    List<GoodsVO> listByCategoryId(Long categoryId, int limit);

    /**
     * 商品搜索
     *
     * @param searchDTO 搜索条件DTO
     * @return 分页结果
     */
    IPage<GoodsCardVO> searchGoods(GoodsSearchDTO searchDTO);

    GoodsDetailVO getGoodsDetail(Long id);

    /**
     * 根据ID和状态查询上架的商品
     *
     * @param goodsId 商品ID
     * @return 商品信息，如果不存在或已下架返回null
     */
    Goods getAvailableGoodsById(Long goodsId);

    /**
     * 扣减商品库存
     *
     * @param goodsId  商品ID
     * @param quantity 扣减数量
     * @return 是否扣减成功
     */
    boolean deductInventory(Long goodsId, Integer quantity);

    List<Goods> getAvailableGoodsByStoreId(Long store);

    // ========== 管理员方法 ==========

    /**
     * 分页查询所有商品（管理员权限）
     *
     * @param page 页码，从1开始
     * @param size 每页数量
     * @return 分页结果
     */
    IPage<Goods> getGoodsPageAdmin(int page, int size);

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
    IPage<Goods> getGoodsPageMerchant(int page, int size);

    /**
     * 根据ID查询商品详情（商家权限 - 验证店铺归属）
     *
     * @param id 商品ID
     * @return 商品信息
     */
    Goods getGoodsDetailMerchant(Long id);

    /**
     * 添加商品（商家权限 - 自动关联当前店铺）
     *
     * @param goods 商品信息
     * @return 是否成功
     */
    boolean addGoodsMerchant(Goods goods);

    /**
     * 修改商品信息（商家权限 - 验证店铺归属）
     *
     * @param goods 商品信息
     * @return 是否成功
     */
    boolean updateGoodsMerchant(Goods goods);

    /**
     * 删除商品（商家权限 - 验证店铺归属）
     *
     * @param id 商品ID
     * @return 是否成功
     */
    boolean removeGoodsMerchant(Long id);
}
