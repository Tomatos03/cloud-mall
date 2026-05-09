package com.cloudmall.framework.models.store;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudmall.framework.models.goods.spu.vo.GoodsCardVO;
import com.cloudmall.framework.models.store.dto.StoreGoodsParamsDTO;
import com.cloudmall.framework.models.store.dto.StoreUpdateDTO;
import com.cloudmall.framework.models.store.vo.StoreVO;

/**
 * 店铺相关业务 Service
 */
public interface IStoreService extends IService<Store> {

    /**
     * 获取店铺基本信息
     * @param storeId 店铺ID
     * @return 店铺基本信息VO
     */
    StoreVO getStoreInfoById(Long storeId);

    /**
     * 分页查询店铺商品列表
     * @param queryDTO 查询条件，包含店铺ID和分页参数
     * @return 商品卡片分页结果
     */
    IPage<GoodsCardVO> pageStoreGoods(StoreGoodsParamsDTO queryDTO);

    /**
     * 根据用户ID获取店铺信息
     * @param userId 用户ID
     * @return 店铺信息
     */
    StoreVO getMyStoreInfo();

    /**
     * 更新店铺信息（部分更新）
     *
     * @param storeId   店铺ID
     * @param userId    用户ID（用于权限校验）
     * @param updateDTO 更新信息
     */
    void updateStore(StoreUpdateDTO updateDTO);

    Store queryStoreByUserId(Long userId);

    StoreInfoDTO getMerchantInfo();
}