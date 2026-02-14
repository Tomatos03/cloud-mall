package com.onlineshop.controller;

import cn.hutool.system.UserInfo;
import com.onlineshop.framework.models.store.IStoreService;
import com.onlineshop.framework.models.store.MerchantInfoDTO;
import com.onlineshop.framework.models.store.dto.StoreUpdateDTO;
import com.onlineshop.framework.models.store.vo.StoreVO;
import com.onlineshop.framework.models.system.user.vo.UserInfoVO;
import com.onlineshop.framework.utils.AuthUserUtils;
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
    public MerchantInfoDTO getMerchantInfo() {
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
     * @param id 店铺 ID
     * @param updateDTO 更新的店铺信息 (支持 name, info, avatarUrl, banner)
     * @return 更新结果
     */
    @PatchMapping("/{id}")
    public void updateStore(@PathVariable Long id, @RequestBody StoreUpdateDTO updateDTO) {
        Long userId = AuthUserUtils.getUserId();
        storeService.updateStore(id, userId, updateDTO);
    }
}