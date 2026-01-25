package com.onlineshop.controller.merchant;

import com.onlineshop.framework.models.store.StoreUpdateDTO;
import com.onlineshop.framework.models.store.IStoreService;
import com.onlineshop.framework.utils.context.UserContextHolder;
import com.onlineshop.framework.models.store.vo.StoreVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 店铺管理控制器
 *
 * @author : Tomatos
 * @date : 2025/12/25
 */
@RestController
@RequestMapping("/manager/merchant/store")
public class MerchantStoreInfoController {

    @Autowired
    private IStoreService storeService;

    /**
     * 获取当前登录商家的店铺信息
     * @return 当前登录商家的店铺信息
     */
    @GetMapping("/me")
    public StoreVO getMyStore() {
        return storeService.getMyStoreInfo();
    }

    /**
     * 获取指定 ID 的店铺详情
     * @param id 店铺 ID
     * @return 店铺详情
     */
    @GetMapping("/{id}")
    public StoreVO getStore(@PathVariable Long id) {
        return storeService.getStoreInfoById(id);
    }

    /**
     * 部分更新店铺信息
     * @param id 店铺 ID
     * @param updateDTO 更新的店铺信息 (支持 name, info, avatarUrl, banner)
     * @return 更新结果
     */
    @PatchMapping("/{id}")
    public void updateStore(@PathVariable Long id, @RequestBody StoreUpdateDTO updateDTO) {
        Long userId = UserContextHolder.getUserId();
        storeService.updateStore(id, userId, updateDTO);
    }
}