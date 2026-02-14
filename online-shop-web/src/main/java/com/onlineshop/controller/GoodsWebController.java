package com.onlineshop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlineshop.framework.models.goods.application.IGoodsAppService;
import com.onlineshop.framework.models.goods.application.vo.WebGoodsDetailVO;
import com.onlineshop.framework.models.goods.spu.IGoodsService;
import com.onlineshop.framework.models.goods.spu.dto.GoodsSearchDTO;
import com.onlineshop.framework.models.goods.spu.vo.GoodsCardVO;
import com.onlineshop.framework.models.goods.spu.vo.GoodsVO;
import com.onlineshop.framework.models.search.application.ISearchAppService;
import com.onlineshop.framework.models.search.service.IGoodsEsService;
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
    private final IGoodsService goodsService;
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
    public List<GoodsVO> listByCategory(
            @RequestParam Long categoryId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return goodsService.listByCategoryId(categoryId, limit);
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
