package com.cloudmall.framework.models.goods.spu;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.framework.common.enums.BizErrorCode;
import com.cloudmall.framework.models.goods.spu.dto.GoodsPageParamsDTO;
import com.cloudmall.framework.models.goods.spu.vo.SpuVO;
import com.cloudmall.framework.utils.AssertUtils;
import com.cloudmall.framework.utils.AuthUserUtils;
import com.cloudmall.framework.utils.image.ImageUtil;
import com.cloudmall.framework.utils.money.Money;

@Service
@RequiredArgsConstructor
public class GoodsService extends ServiceImpl<GoodsMapper, Goods> implements IGoodsService {
    @Override
    public List<Goods> queryEnableGoodsList() {
        Long storeId = AuthUserUtils.getStoreId();
        return lambdaQuery()
                .eq(storeId != null, Goods::getStoreId, storeId)
                .eq(Goods::getStatus, true)
                .list();
    }

    @Override
    public List<Goods> queryGoodsListByIds(Collection<? extends Serializable> ids) {
        if (CollectionUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return listByIds(ids);
    }

    @Override
    public IPage<SpuVO> pageGoods(GoodsPageParamsDTO queryDTO) {
        Page<Goods> pageObj = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        return this.lambdaQuery()
                   .eq(queryDTO.getStoreId() != null, Goods::getStoreId, queryDTO.getStoreId())
                   .eq(queryDTO.getStatus() != null, Goods::getStatus, queryDTO.getStatus())
                   .page(pageObj)
                   .convert(this::convertSpuVO);
    }

    private SpuVO convertSpuVO(Goods goods) {
        return SpuVO.builder()
                    .goodsId(goods.getId())
                    .goodsName(goods.getName())
                    .storeName(goods.getStoreName())
                    .categoryIdPath(convertCateIdPathList(goods.getCategoryIdPath()))
                    .displayImageUrls(ImageUtil.createImageUrlList(goods.getDisplayImages()))
                    .minPrice(Money.ofCents(goods.getMinPrice())
                                   .toYuanString())
                    .maxPrice(Money.ofCents(goods.getMaxPrice())
                                   .toYuanString())
                    .status(goods.getStatus())
                    .sellPoint(goods.getSellPoint())
                    .unitName(goods.getUnitName())
                    .unitId(goods.getUnitId())
                    .categoryId(goods.getCategoryId())
                    .auditStatus(goods.getAuditStatus())
                    .build();
    }

    private static List<Integer> convertCateIdPathList(@NotNull String categoryIdPathStr) {
        return Stream.of(categoryIdPathStr.split("/"))
                     .map(Integer::valueOf)
                     .toList();
    }

    @Override
    public void updateGoodsStatus(Long goodsId, Boolean status) {
        this.lambdaUpdate()
            .eq(Goods::getId, goodsId)
            .set(Goods::getStatus, status)
            .update();
    }

    @Override
    public void increaseSales(Long goodsId, Integer quantity) {
        boolean updated = lambdaUpdate().eq(Goods::getId, goodsId)
                                        .setSql("sales = IFNULL(sales, 0) + " + quantity)
                                        .update();
        AssertUtils.isTrue(updated, BizErrorCode.GOODS_UPDATE_FAILED);
    }

    @Override
    public List<Goods> queryGoodsByMultipleCategoryIds(List<Long> categoryIds, int limit) {
        if (CollectionUtil.isEmpty(categoryIds)) {
            return Collections.emptyList();
        }

        // 一次查询所有分类的商品，按销量降序排列
        return lambdaQuery()
                .eq(Goods::getStatus, true)
                .in(Goods::getCategoryId, categoryIds)
                .orderByDesc(Goods::getSales)
                .last("limit " + limit)
                .list();
    }
}
