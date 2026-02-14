package com.onlineshop.framework.models.favorite;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlineshop.framework.models.favorite.dto.FavoriteParamsDTO;

import com.baomidou.mybatisplus.extension.service.IService;
import com.onlineshop.framework.models.favorite.dto.FavoriteStatusDTO;
import com.onlineshop.framework.models.favorite.vo.FavoriteVO;

import java.util.List;

public interface IFavoriteService extends IService<Favorite> {
    List<Favorite> queryLast7DaysFavoritesByGoodsIds(List<Long> goodsIds);

    /**
     * 分页查询当前用户的收藏
     *
     * @param queryDTO 分页查询条件
     * @return 分页结果
     */
    IPage<FavoriteVO> pageUserFavorites(FavoriteParamsDTO queryDTO);

    /**
     * 添加收藏
     *
     * @param goodsId 商品id
     */
    void addFavorite(Long goodsId);

    /**
     * 检查某个商品是否已被当前用户收藏
     *
     * @param goodsId 商品id
     * @return true-已收藏 false-未收藏
     */
    FavoriteStatusDTO isFavorite(Long goodsId);
}

