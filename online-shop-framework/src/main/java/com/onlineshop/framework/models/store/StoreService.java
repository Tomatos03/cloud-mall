package com.onlineshop.framework.models.store;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineshop.framework.models.goods.spu.Goods;
import com.onlineshop.framework.models.goods.spu.IGoodsService;
import com.onlineshop.framework.models.goods.spu.vo.GoodsCardVO;
import com.onlineshop.framework.models.store.dto.StoreGoodsParamsDTO;
import com.onlineshop.framework.models.store.dto.StoreUpdateDTO;
import com.onlineshop.framework.models.store.vo.StoreVO;
import com.onlineshop.framework.models.system.user.IUserService;
import com.onlineshop.framework.models.system.user.entity.User;
import com.onlineshop.framework.utils.AuthUserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 店铺相关业务 Service 实现
 */
@Service
@RequiredArgsConstructor
public class StoreService extends ServiceImpl<StoreMapper, Store> implements IStoreService {
    private final IGoodsService goodsService;
    private final IUserService userService;

    @Override
    public StoreVO getStoreInfoById(Long storeId) {
        Store store = getById(storeId);
        return buildStoreItemVO(store);
    }

    @Override
    public IPage<GoodsCardVO> pageStoreGoods(StoreGoodsParamsDTO queryDTO) {
        // 构建查询条件
        QueryWrapper<Goods> wrapper = new QueryWrapper<>();
        wrapper.eq("store_id", queryDTO.getStoreId())
               .eq("status", true);

        Page<Goods> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());

        return goodsService.page(page, wrapper)
                           .convert(GoodsCardVO::convertGoodsCardVO);
    }

    @Override
    public StoreVO getMyStoreInfo() {
        Store store = queryStoreByUserId(AuthUserUtils.getUserId());
        return buildStoreItemVO(store);
    }

    @Override
    public void updateStore(StoreUpdateDTO updateDTO) {
        LambdaUpdateWrapper<Store> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Store::getId, AuthUserUtils.getStoreId())
                     .eq(Store::getUserId, AuthUserUtils.getUserId());

        updateWrapper.set(Objects.nonNull(updateDTO.getName()), Store::getName, updateDTO.getName());
        updateWrapper.set(Objects.nonNull(updateDTO.getInfo()), Store::getInfo, updateDTO.getInfo());
        updateWrapper.set(Objects.nonNull(updateDTO.getAvatarUrl()), Store::getAvatarUrl, updateDTO.getAvatarUrl());
        updateWrapper.set(Objects.nonNull(updateDTO.getBanner()), Store::getBanner, updateDTO.getBanner());

        update(updateWrapper);
    }

    @Override
    public Store queryStoreByUserId(Long userId) {
        return lambdaQuery().eq(Store::getUserId, userId)
                            .one();
    }

    @Override
    public StoreInfoDTO getMerchantInfo() {
        Store store = queryStoreByUserId(AuthUserUtils.getUserId());
        User user = userService.getById(AuthUserUtils.getUserId());
        return StoreInfoDTO.builder()
                           .uid(user.getId()
                                    .toString())
                           .storeId(store.getId())
                           .storeName(store.getName())
                           .nickname(user.getNickname())
                           .username(user.getUsername())
                           .avatarUrl(user.getAvatarUrl())
                           .build();
    }

    /**
     * 构建 StoreItemVO
     */
    private StoreVO buildStoreItemVO(Store store) {
        Objects.requireNonNull(store);

        return StoreVO.builder()
                      .id(String.valueOf(store.getId()))
                      .name(store.getName())
                      .description(store.getInfo())
                      .avatarUrl(store.getAvatarUrl())
                      .banner(store.getBanner())
                      .build();
    }
}