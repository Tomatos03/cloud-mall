package com.cloudmall.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cloudmall.framework.models.goods.application.IGoodsAppService;
import com.cloudmall.framework.models.goods.application.vo.WebGoodsDetailVO;
import com.cloudmall.framework.models.goods.spu.Goods;
import com.cloudmall.framework.models.goods.spu.dto.GoodsSearchDTO;
import com.cloudmall.framework.models.goods.spu.vo.GoodsCardVO;
import com.cloudmall.framework.models.search.application.ISearchAppService;
import com.cloudmall.framework.models.search.service.IGoodsEsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *
 *
 * @author : Tomatos
 * @date : 2025/12/22
 */
@RestController
@RequestMapping("/web/goods")
@RequiredArgsConstructor
public class GoodsWebController {
    private final IGoodsAppService goodsAppService;
    private final ISearchAppService searchAppService;
    private final IGoodsEsService goodsEsService;

    /**
     * 根据分类ID获取商品列表
     *
     * @param categoryId 分类ID
     * @param limit      返回数量限制，默认10
     * @return 商品列表
     */
    @GetMapping("/listByCategory")
    public List<Goods> listByCategory(
            @RequestParam Long categoryId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return goodsAppService.queryGoodsByCategoryId(categoryId, limit);
    }

    /**
     * 商品搜索接口
     *
     * @param searchDTO 搜索条件DTO
     * @return 分页商品列表
     */
    @GetMapping("/search")
    public IPage<GoodsCardVO> searchGoods(GoodsSearchDTO searchDTO) {
        return searchAppService.searchGoods(searchDTO);
    }

    /**
     * 店铺内商品搜索
     *
     * @param storeId   店铺ID
     * @param searchDTO 搜索条件DTO
     * @return 分页商品列表
     */
    @GetMapping("/store/{storeId}/search")
    public IPage<GoodsCardVO> searchGoodsInStore(
            @PathVariable Long storeId,
            GoodsSearchDTO searchDTO
    ) {
        searchDTO.setStoreId(storeId);
        return searchAppService.searchGoods(searchDTO);
    }

    @GetMapping("/detail/{id}")
    public WebGoodsDetailVO getGoodsDetail(@PathVariable Long id) {
        return goodsAppService.getWebGoodsDetail(id);
    }

    // TODO: 方便开发, 后续删除
    @GetMapping("/rebuild")
    public void rebuildGoodsIndex() {
        goodsEsService.rebuildAllGoodsIndex();
    }
}
