package com.onlineshop.framework.models.goods.spu;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineshop.framework.models.audit.enums.AuditStatus;
import com.onlineshop.framework.models.category.Category;
import com.onlineshop.framework.models.category.ICategoryService;
import com.onlineshop.framework.models.goods.spu.vo.GoodsVO;
import com.onlineshop.framework.models.goods.spu.vo.SpuVO;
import com.onlineshop.framework.utils.AuthUserUtils;
import com.onlineshop.framework.utils.image.ImageUtil;
import com.onlineshop.framework.utils.money.Money;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class GoodsService extends ServiceImpl<GoodsMapper, Goods> implements IGoodsService {
    private final ICategoryService categoryService;

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
    public List<GoodsVO> listByCategoryId(Long categoryId, int limit) {
        return this.lambdaQuery()
                   .eq(Goods::getStatus, true)
                   .last("limit " + limit)
                   .in(Goods::getCategoryId, getLeafCategoryById(categoryId))
                   .list()
                   .stream()
                   .map(GoodsVO::convertToGoodsVO)
                   .toList();
    }

    @Override
    public IPage<SpuVO> pageQuery(int page, int size) {
        Page<Goods> pageObj = new Page<>(page, size);
        return this.page(pageObj, buildQueryWrapper())
                   .convert(this::convertSpuVO);
    }

    private static LambdaQueryWrapper<Goods> buildQueryWrapper() {
        Long storeId = AuthUserUtils.getStoreId();
        return new LambdaQueryWrapper<Goods>()
                .eq(storeId != null, Goods::getStoreId, AuthUserUtils.getStoreId());
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
        Goods goods = getById(goodsId);
        if (goods != null) {
            goods.setSales(goods.getSales() + quantity);
            updateById(goods);
        }
    }

    @Override
    public void updateGoodsAuditStatus(Long goodsId, AuditStatus status) {
        lambdaUpdate().eq(Goods::getId, goodsId)
                      .set(Goods::getAuditStatus, status.getCode())
                      .update();
    }

    private List<Long> getLeafCategoryById(Long categoryId) {
        List<Long> allCategoryIds = new ArrayList<>();
        allCategoryIds.add(categoryId);

        Queue<Long> queue = new LinkedList<>();
        queue.add(categoryId);
        while (!queue.isEmpty()) {
            Long currentId = queue.poll();
            List<Category> children = categoryService.list(
                    new QueryWrapper<Category>().eq("parent_id", currentId)
            );
            for (Category child : children) {
                allCategoryIds.add(child.getId());
                queue.add(child.getId());
            }
        }
        return allCategoryIds;
    }
}
