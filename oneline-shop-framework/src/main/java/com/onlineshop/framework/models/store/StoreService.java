package com.onlineshop.framework.models.store;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineshop.framework.models.goods.spu.Goods;
import com.onlineshop.framework.models.goods.spu.GoodsMapper;
import com.onlineshop.framework.models.store.vo.StoreItemVO;
import com.onlineshop.framework.models.store.vo.StoreProductItemVO;
import com.onlineshop.framework.utils.context.UserContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 店铺相关业务 Service 实现
 */
@Service
public class StoreService extends ServiceImpl<StoreMapper, Store> implements IStoreService {

    @Autowired
    private StoreMapper storeMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Override
    public StoreItemVO getStoreInfo(Long storeId) {
        Store store = storeMapper.selectById(storeId);
        return store == null ? null : buildStoreItemVO(store);
    }


    @Override
    public List<StoreProductItemVO> getStoreProducts(Long storeId) {
        QueryWrapper<Goods> wrapper = new QueryWrapper<>();
        wrapper.eq("store_id", storeId);
        List<Goods> goodsList = goodsMapper.selectList(wrapper);
        return goodsList.stream()
                        .map(goods -> StoreProductItemVO.builder()
                                                        .id(String.valueOf(goods.getId()))
                                                        .title(goods.getName())
                                                        .desc(goods.getInfo())
                                                        .price(goods.getPrice())
                                                        .img(goods.getImg())
                                                        .sale(goods.getSales() == null ? 0 :
                                                                      goods.getSales()
                                                                                                  .intValue())
                                                        .build()
                        )
                        .collect(Collectors.toList());
    }

    @Override
    public StoreItemVO getMyStore(Long userId) {
        QueryWrapper<Store> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        Store store = storeMapper.selectOne(wrapper);
        return store == null ? null : buildStoreItemVO(store);
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

        int result = storeMapper.update(null, updateWrapper);
        return result > 0;
    }

    /**
     * 构建 StoreItemVO
     */
    private StoreItemVO buildStoreItemVO(Store store) {
        return StoreItemVO.builder()
                          .id(String.valueOf(store.getId()))
                          .name(store.getName())
                          .info(store.getInfo())
                          .avatarUrl(store.getAvatarUrl())
                          .banner(store.getBanner())
                          .build();
    }

    public Store queryUserStore() {
        return lambdaQuery().eq(Store::getUserId, UserContextHolder.getUserId())
                            .one();
    }
}