package com.onlineshop.framework.models.goods.spu;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineshop.framework.models.category.Category;
import com.onlineshop.framework.models.category.ICategoryService;
import com.onlineshop.framework.models.goods.spu.vo.GoodsVO;
import com.onlineshop.framework.models.goods.spu.vo.SpuVO;
import com.onlineshop.framework.utils.context.UserContextHolder;
import com.onlineshop.framework.utils.image.ImageUtil;
import com.onlineshop.framework.utils.money.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Stream;

@Service
public class GoodsService extends ServiceImpl<GoodsMapper, Goods> implements IGoodsService {
    @Autowired
    private ICategoryService categoryService;

    @Override
    public boolean addGoods(Goods goods) {
        return save(goods);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateGoods(Goods goods) {
        return this.updateById(goods);
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
    public IPage<SpuVO> getGoodsPageAdmin(int page, int size) {
        QueryWrapper<Goods> queryWrapper = new QueryWrapper<>();
        Page<Goods> pageObj = new Page<>(page, size);
        return this.page(pageObj, queryWrapper)
                   .convert(this::convertSpuVO);
    }

    @Override
    public IPage<Goods> getStoreGoodsPageAdmin(Long storeId, int page, int size) {
        QueryWrapper<Goods> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("store_id", storeId);
        Page<Goods> pageObj = new Page<>(page, size);
        return this.page(pageObj, queryWrapper);
    }

    @Override
    public IPage<SpuVO> getGoodsPageMerchant(int page, int size) {
        QueryWrapper<Goods> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("store_id", UserContextHolder.getStoreId());

        Page<Goods> pageObj = new Page<>(page, size);

        IPage<Goods> goodsPage = this.page(pageObj, queryWrapper);

        return goodsPage.convert(this::convertSpuVO);
    }

    @Override
    public void updateGoodsShelfStatus(Long goodsId, Boolean status) {
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

    private SpuVO convertSpuVO(Goods goods) {
        List<Integer> categoryIdPath = Stream.of(goods.getCategoryIdPath()
                                                      .split("/")
                                             )
                                             .map(Integer::valueOf)
                                             .toList();
        return SpuVO.builder()
                    .goodsId(goods.getId())
                    .goodsName(goods.getName())
                    .storeName(goods.getStoreName())
                    .categoryIdPath(categoryIdPath)
                    .displayImageUrls(ImageUtil.createImageUrlList(goods.getDisplayImages()))
                    .minPrice(Money.ofCents(goods.getMinPrice()).toYuanString())
                    .maxPrice(Money.ofCents(goods.getMaxPrice()).toYuanString())
                    .status(goods.getStatus())
                    .sellPoint(goods.getSellPoint())
                    .unitName(goods.getUnitName())
                    .unitId(goods.getUnitId())
                    .categoryId(goods.getCategoryId())
                    .build();
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
