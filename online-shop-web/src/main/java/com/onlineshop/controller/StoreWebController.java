package com.onlineshop.controller;

import com.onlineshop.framework.models.store.vo.StoreItemVO;
import com.onlineshop.framework.models.store.vo.StoreProductItemVO;
import com.onlineshop.framework.models.store.IStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 店铺相关接口
 */
@RestController
@RequestMapping("/web/store")
public class StoreWebController {

    @Autowired
    private IStoreService storeService;

    /**
     * 获取店铺基本信息
     * GET /store/info/{storeId}
     */
    @GetMapping("/info/{storeId}")
    public StoreItemVO getStoreInfo(@PathVariable Long storeId) {
        return storeService.getStoreInfo(storeId);
    }

    /**
     * 获取店铺商品列表
     * GET /store/products/{storeId}
     */
    @GetMapping("/products/{storeId}")
    public List<StoreProductItemVO> getStoreProducts(@PathVariable Long storeId) {
        return storeService.getStoreProducts(storeId);
    }
}