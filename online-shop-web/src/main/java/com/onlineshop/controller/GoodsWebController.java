package com.onlineshop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlineshop.framework.models.goods.IGoodsService;
import com.onlineshop.framework.models.goods.dto.GoodsSearchDTO;
import com.onlineshop.framework.models.goods.vo.GoodsCardVO;
import com.onlineshop.framework.models.goods.vo.GoodsDetailVO;
import com.onlineshop.framework.models.goods.vo.GoodsVO;
import org.springframework.beans.factory.annotation.Autowired;
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
public class GoodsWebController {

    @Autowired
    private IGoodsService goodsService;

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
        return goodsService.searchGoods(searchDTO);
    }

    @GetMapping("/detail/{id}")
    public GoodsDetailVO getGoodsDetail(@PathVariable Long id) {
        return goodsService.getGoodsDetail(id);
    }
}
