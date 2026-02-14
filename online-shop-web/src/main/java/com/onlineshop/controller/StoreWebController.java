package com.onlineshop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlineshop.framework.models.goods.spu.vo.GoodsCardVO;
import com.onlineshop.framework.models.store.IStoreService;
import com.onlineshop.framework.models.store.dto.StoreGoodsParamsDTO;
import com.onlineshop.framework.models.store.vo.StoreVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public StoreVO getStoreInfo(@PathVariable Long storeId) {
        return storeService.getStoreInfoById(storeId);
    }

    /**
     * 分页获取店铺商品列表
     */
    @GetMapping("/goods")
    public IPage<GoodsCardVO> pageStoreGoods(StoreGoodsParamsDTO queryDTO) {
        return storeService.pageStoreGoods(queryDTO);
    }
}