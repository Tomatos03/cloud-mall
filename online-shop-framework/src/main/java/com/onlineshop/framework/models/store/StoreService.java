package com.onlineshop.framework.models.store;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
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

        Page<Goods> page = new Page<>(queryDTO.getPageNo(), queryDTO.getPageSize());

        return goodsService.page(page, wrapper)
                           .convert(GoodsCardVO::convertGoodsCardVO);
    }

    @Override
    public StoreVO getMyStoreInfo() {
        Store store = queryStoreByUserId(AuthUserUtils.getUserId());
        return buildStoreItemVO(store);
    }

    @Override
    public boolean updateStore(Long storeId, Long userId, StoreUpdateDTO updateDTO) {
        // 使用 UpdateWrapper 构建条件：店铺ID且用户ID匹配（权限校验）
        UpdateWrapper<Store> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", storeId)
                     .eq("user_id", userId);

        // 仅更新非空字段
        if (Objects.nonNull(updateDTO.getName())) {
            updateWrapper.set("name", updateDTO.getName());
        }
        if (Objects.nonNull(updateDTO.getInfo())) {
            updateWrapper.set("info", updateDTO.getInfo());
        }
        if (Objects.nonNull(updateDTO.getAvatarUrl())) {
            updateWrapper.set("avatar_url", updateDTO.getAvatarUrl());
        }
        if (Objects.nonNull(updateDTO.getBanner())) {
            updateWrapper.set("banner", updateDTO.getBanner());
        }

        return update(updateWrapper);
    }

    @Override
    public Store queryStoreByUserId(Long userId) {
        return lambdaQuery().eq(Store::getUserId, userId)
                            .one();
    }

    @Override
    public MerchantInfoDTO getMerchantInfo() {
        Store store = queryStoreByUserId(AuthUserUtils.getUserId());
        User user = userService.getById(AuthUserUtils.getUserId());
        return MerchantInfoDTO.builder()
                              .uid(user.getId().toString())
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

    public Store queryUserStore() {
        return lambdaQuery().eq(Store::getUserId, AuthUserUtils.getUserId())
                            .one();
    }
}