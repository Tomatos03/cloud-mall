package com.cloudmall.controller;

import com.cloudmall.framework.models.store.IStoreService;
import com.cloudmall.framework.models.store.StoreInfoDTO;
import com.cloudmall.framework.models.store.dto.StoreUpdateDTO;
import com.cloudmall.framework.models.store.vo.StoreVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 店铺管理控制器
 *
 * @author : Tomatos
 * @date : 2025/12/25
 */
@RestController
@RequestMapping("/merchant/store")
public class StoreMerchantController {
    @Autowired
    private IStoreService storeService;

    @GetMapping("/info")
    public StoreInfoDTO getMerchantInfo() {
        return storeService.getMerchantInfo();
    }

    /**
     * 获取当前登录商家的店铺信息
     * @return 当前登录商家的店铺信息
     */
    @GetMapping
    public StoreVO getMyStore() {
        return storeService.getMyStoreInfo();
    }

    /**
     * 部分更新店铺信息
     * @param updateDTO 更新的店铺信息 (支持 name, info, avatarUrl, banner)
     * @return 更新结果
     */
    @PostMapping
    public void updateStore(@RequestBody StoreUpdateDTO updateDTO) {
        storeService.updateStore(updateDTO);
    }
}