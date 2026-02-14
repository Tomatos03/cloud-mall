package com.onlineshop.framework.models.goods.spu;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.onlineshop.framework.models.audit.enums.AuditStatus;
import com.onlineshop.framework.models.goods.spu.vo.GoodsVO;
import com.onlineshop.framework.models.goods.spu.vo.SpuVO;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public interface IGoodsService extends IService<Goods> {
    List<Goods> queryEnableGoodsList();

    List<Goods> queryGoodsListByIds(Collection<? extends Serializable> ids);

    List<GoodsVO> listByCategoryId(Long categoryId, int limit);

    /**
     * 分页查询商品（管理员/商家权限，自动区分）
     *
     * @param page 页码，从1开始
     * @param size 每页数量
     * @return 分页结果
     */
    IPage<SpuVO> pageQuery(int page, int size);

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

    void updateGoodsAuditStatus(Long goodsId, AuditStatus auditStatus);
}
