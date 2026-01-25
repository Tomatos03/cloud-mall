package com.onlineshop.framework.models.favorite;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineshop.framework.models.favorite.dto.FavoriteQueryDTO;
import com.onlineshop.framework.models.favorite.dto.FavoriteStatusDTO;
import com.onlineshop.framework.models.favorite.vo.FavoriteVO;
import com.onlineshop.framework.models.goods.spu.Goods;
import com.onlineshop.framework.models.goods.spu.IGoodsService;
import com.onlineshop.framework.utils.context.UserContextHolder;
import com.onlineshop.framework.utils.image.ImageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FavoriteService extends ServiceImpl<FavoriteMapper, Favorite> implements IFavoriteService {
    @Autowired
    private IGoodsService goodsService;

    @Override
    public IPage<FavoriteVO> pageUserFavorites(FavoriteQueryDTO queryDTO) {
        Long userId = UserContextHolder.getUserId();

        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
               .orderByDesc(Favorite::getAddedAt);

        return this.page(new Page<>(queryDTO.getPageNo(), queryDTO.getPageSize()), wrapper)
                   .convert(FavoriteVO::convertFavoriteVO);
    }

    @Override
    public void addFavorite(Long goodsId) {
        Goods goods = goodsService.getById(goodsId);
        if (goods == null) {
            throw new RuntimeException("商品不存在");
        }

        Favorite favorite = buildFavorite(goods);
        this.save(favorite);
    }

    @Override
    public FavoriteStatusDTO isFavorite(Long goodsId) {
        Favorite favorite = this.lambdaQuery()
                                .eq(Favorite::getUserId, UserContextHolder.getUserId())
                                .eq(Favorite::getGoodsId, goodsId)
                                .one();
        FavoriteStatusDTO favoriteStatusDTO = new FavoriteStatusDTO();
        favoriteStatusDTO.setFavorite(favorite != null);
        return favoriteStatusDTO;
    }

    /**
     * 构建收藏对象
     *
     * @param goods 商品信息
     * @return 收藏对象
     */
    private Favorite buildFavorite(Goods goods) {
        return Favorite.builder()
                       .userId(UserContextHolder.getUserId())
                       .goodsId(goods.getId())
                       .goodsName(goods.getName())
                       .goodsMainImageUrl(ImageUtil.getMainImageUrl(goods.getDisplayImages()))
                       .goodsSellPoint(goods.getSellPoint())
                       .storeId(goods.getStoreId())
                       .goodsPrice(goods.getMinPrice())
                       .build();
    }
}
