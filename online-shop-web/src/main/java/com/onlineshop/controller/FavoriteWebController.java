package com.onlineshop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlineshop.framework.models.favorite.Favorite;
import com.onlineshop.framework.models.favorite.IFavoriteService;
import com.onlineshop.framework.models.favorite.dto.FavoriteQueryDTO;
import com.onlineshop.framework.models.favorite.dto.FavoriteStatusDTO;
import com.onlineshop.framework.models.favorite.vo.FavoriteVO;
import com.onlineshop.framework.utils.context.UserContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    // 分页获取当前用户所有收藏
    @GetMapping
    public IPage<FavoriteVO> pageFavorites(FavoriteQueryDTO queryDTO) {
        return favoriteService.pageUserFavorites(queryDTO);
    }

    // 添加收藏
    @PostMapping("{goodsId}")
    public void addFavorite(@PathVariable Long goodsId) {
        favoriteService.addFavorite(goodsId);
    }

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
