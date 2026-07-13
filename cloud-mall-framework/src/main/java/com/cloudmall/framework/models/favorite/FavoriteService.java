package com.cloudmall.framework.models.favorite;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.framework.common.enums.BizErrorCode;
import com.cloudmall.framework.models.favorite.dto.FavoriteParamsDTO;
import com.cloudmall.framework.models.favorite.dto.FavoriteStatusDTO;
import com.cloudmall.framework.models.favorite.vo.FavoriteVO;
import com.cloudmall.framework.models.goods.spu.Goods;
import com.cloudmall.framework.models.goods.spu.IGoodsService;
import com.cloudmall.framework.utils.AssertUtils;
import com.cloudmall.framework.context.AuthUserContext;
import com.cloudmall.framework.utils.image.ImageUtil;

@Service
@RequiredArgsConstructor
public class FavoriteService extends ServiceImpl<FavoriteMapper, Favorite> implements IFavoriteService {
    private final IGoodsService goodsService;

    @Override
    public List<Favorite> queryLast7DaysFavoritesByGoodsIds(List<Long> goodsIds) {
        if (CollUtil.isEmpty(goodsIds)) {
            return Collections.emptyList();
        }

        LocalDate sevenDaysAgo = LocalDate.now()
                                          .minusDays(6);
        LocalDateTime startDateTime = sevenDaysAgo.atStartOfDay();
        return this.lambdaQuery()
                   .in(Favorite::getGoodsId, goodsIds)
                   .ge(Favorite::getAddedAt, startDateTime)
                   .list();
    }

    @Override
    public IPage<FavoriteVO> pageUserFavorites(FavoriteParamsDTO queryDTO) {
        Long userId = AuthUserContext.getUserId();

        return this.lambdaQuery()
                   .eq(Favorite::getUserId, userId)
                   .orderByDesc(Favorite::getAddedAt)
                   .page(new Page<>(queryDTO.getPage(), queryDTO.getPageSize()))
                   .convert(FavoriteVO::convertFavoriteVO);
    }

    @Override
    public void addFavorite(Long goodsId) {
        Goods goods = goodsService.getById(goodsId);
        AssertUtils.notNull(goods, BizErrorCode.GOODS_OR_SHOP_NOT_EXIST);

        Favorite favorite = buildFavorite(goods);
        this.save(favorite);
    }

    @Override
    public void removeFavorite(Long goodsId) {
        Long userId = AuthUserContext.getUserId();
        this.lambdaUpdate()
            .eq(Favorite::getUserId, userId)
            .eq(Favorite::getGoodsId, goodsId)
            .remove();
    }

    @Override
    public FavoriteStatusDTO isFavorite(Long goodsId) {
        Favorite favorite = this.lambdaQuery()
                                .eq(Favorite::getUserId, AuthUserContext.getUserId())
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
                       .userId(AuthUserContext.getUserId())
                       .goodsId(goods.getId())
                       .goodsName(goods.getName())
                       .goodsMainImageUrl(ImageUtil.getMainImageUrl(goods.getDisplayImages()))
                       .goodsSellPoint(goods.getSellPoint())
                       .storeId(goods.getStoreId())
                       .goodsPrice(goods.getMinPrice())
                       .build();
    }
}
