package com.onlineshop.controller;

import com.onlineshop.framework.models.favorite.dto.AddFavoriteDTO;
import com.onlineshop.framework.models.favorite.dto.FavoriteStatusDTO;
import com.onlineshop.framework.models.favorite.Favorite;
import com.onlineshop.framework.models.favorite.IFavoriteService;
import com.onlineshop.framework.utils.context.UserContextHolder;
import com.onlineshop.framework.models.goods.spu.IGoodsService;
import com.onlineshop.framework.models.goods.spu.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *
 *
 * @author : Tomatos
 * @date : 2025/12/23
 */
@RestController
@RequestMapping("/web/favorites")
public class FavoriteWebController {
    @Autowired
    private IFavoriteService favoriteService;

    @Autowired
    private IGoodsService goodsService;

    // 获取当前用户所有收藏
    @GetMapping
    public List<Favorite> listFavorites() {
        Long userId = UserContextHolder.getUserId();
        return favoriteService.lambdaQuery()
                .eq(Favorite::getUserId, userId)
                .list();
    }

    // 添加收藏
    @PostMapping
    public Favorite addFavorite(@RequestBody AddFavoriteDTO addFavoriteDTO) {
        Long userId = UserContextHolder.getUserId();
        Long goodsId = addFavoriteDTO.getGoodsId();
        Long storeId = addFavoriteDTO.getStoreId();
        Goods goods = goodsService.getById(goodsId);
        if (goods == null) {
            throw new RuntimeException("商品不存在");
        }
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setGoodsId(goodsId);
        favorite.setGoodsTitle(goods.getName());
        favorite.setGoodsImg(goods.getImg());
        favorite.setGoodsPrice(goods.getPrice());
        favorite.setGoodsDesc(goods.getDescription());
        favorite.setStoreId(storeId);
        favoriteService.save(favorite);
        return favorite;
    }

    // 取消收藏
    @DeleteMapping("{id}")
    public boolean removeFavorite(@PathVariable Long id) {
        return favoriteService.removeById(id);
    }

    // 检查收藏状态
    @GetMapping("/status/{goodsId}")
    public FavoriteStatusDTO checkFavoriteStatus(@PathVariable Long goodsId) {
        Long userId = UserContextHolder.getUserId();
        Favorite one = favoriteService.lambdaQuery()
                                      .eq(Favorite::getUserId, userId)
                                      .eq(Favorite::getGoodsId, goodsId)
                                      .one();
        FavoriteStatusDTO favoriteStatusDTO = new FavoriteStatusDTO();
        if (one != null) {
            favoriteStatusDTO.setFavoriteId(one.getId());
            favoriteStatusDTO.setFavorite(true);
        }
        return favoriteStatusDTO;
    }
}
