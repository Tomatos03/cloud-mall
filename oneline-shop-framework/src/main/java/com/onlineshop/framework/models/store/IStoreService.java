package com.onlineshop.framework.models.store;

import com.baomidou.mybatisplus.extension.service.IService;
import com.onlineshop.framework.models.store.vo.StoreItemVO;
import com.onlineshop.framework.models.store.vo.StoreProductItemVO;

import java.util.List;

/**
 * 店铺相关业务 Service
 */
public interface IStoreService extends IService<Store> {

    /**
     * 获取店铺基本信息
     * @param storeId 店铺ID
     * @return 店铺基本信息VO
     */
    StoreItemVO getStoreInfo(Long storeId);

    /**
     * 获取店铺商品列表
     * @param storeId 店铺ID
     * @return 商品列表
     */
    List<StoreProductItemVO> getStoreProducts(Long storeId);

    /**
     * 根据用户ID获取店铺信息
     * @param userId 用户ID
     * @return 店铺信息
     */
    StoreItemVO getMyStore(Long userId);

    /**
     * 更新店铺信息（部分更新）
     * @param storeId 店铺ID
     * @param userId 用户ID（用于权限校验）
     * @param updateDTO 更新信息
     * @return 更新是否成功
     */
    boolean updateStore(Long storeId, Long userId, StoreUpdateDTO updateDTO);
}