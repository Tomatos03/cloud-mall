package com.onlineshop.framework.models.goods.spu;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineshop.framework.enums.BizErrorCode;
import com.onlineshop.framework.exception.BusinessException;
import com.onlineshop.framework.models.category.Category;
import com.onlineshop.framework.models.category.ICategoryService;
import com.onlineshop.framework.models.goods.spu.dto.GoodsSearchDTO;
import com.onlineshop.framework.models.goods.spu.vo.GoodsCardVO;
import com.onlineshop.framework.models.goods.spu.vo.GoodsDetailVO;
import com.onlineshop.framework.models.goods.spu.vo.GoodsVO;
import com.onlineshop.framework.models.store.IStoreService;
import com.onlineshop.framework.models.store.Store;
import com.onlineshop.framework.utils.context.UserContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Service
public class GoodsService extends ServiceImpl<GoodsMapper, Goods> implements IGoodsService {
    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private ICategoryService categoryService;
    @Autowired
    private IStoreService storeService;

    @Override
    public boolean addGoods(Goods goods) {
        return save(goods);
    }

    @Override
    public IPage<Goods> getGoodsPage(int page, int size) {
        Long storeId = UserContextHolder.getStoreId();
        QueryWrapper<Goods> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("store_id", storeId);

        Page<Goods> pageObj = new Page<>(page, size);
        return this.page(pageObj, queryWrapper);
    }

    private static void checkStore(Store store) {
        if (store == null) {
            throw new BusinessException(BizErrorCode.STORE_NOT_EXIST);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateGoods(Goods goods) {
        return this.updateById(goods);
    }

    @Override
    public List<Goods> queryByIds(List<Long> goodsIds) {
        return goodsMapper.selectByIds(goodsIds);
    }

    @Override
    public List<GoodsVO> listByCategoryId(Long categoryId, int limit) {
        return this.lambdaQuery()
                   .eq(Goods::getStatus, true)
                   .last("limit " + limit)
                   .in(Goods::getCategoryId, getLeafCategoryById(categoryId))
                   .list()
                   .stream()
                   .map(goods -> BeanUtil.copyProperties(goods, GoodsVO.class))
                   .toList();
    }

    @Override
    public IPage<GoodsCardVO> searchGoods(GoodsSearchDTO searchDTO) {
        // 获取分类的所有叶子节点ID
        List<Long> leafCategoryIds = searchDTO.getCategoryId() != null
                ? getLeafCategoryById(searchDTO.getCategoryId())
                : null;

        // 直接使用DTO和分类列表构建搜索条件
        QueryWrapper<Goods> queryWrapper = GoodsCardVO.buildSearchWrapper(
                leafCategoryIds,
                searchDTO
        );

        Page<Goods> pageObj = new Page<>(searchDTO.getPageNum(), searchDTO.getPageSize());
        IPage<Goods> goodsPage = this.page(pageObj, queryWrapper);

        return goodsPage.convert(GoodsCardVO::fromGoods);
    }

    @Override
    public GoodsDetailVO getGoodsDetail(Long id) {
        Goods goods = this.lambdaQuery()
                          .eq(Goods::getId, id)
                          .one();

        Store store = storeService.lambdaQuery()
                                  .eq(Store::getId, goods.getStoreId())
                                  .one();

        return buildGoodsDetailVO(goods, store);
    }

    private GoodsDetailVO buildGoodsDetailVO(Goods goods, Store store) {
        if (goods == null || store == null) {
            throw new BusinessException(BizErrorCode.GOODS_OR_SHOP_NOT_EXIST);
        }
        List<String> subImgList = List.of(goods.getImgList()
                                               .split(","));
        return GoodsDetailVO.builder()
                            .storeId(store.getId())
                            .storeName(store.getName())
                            .storeAvatarUrl(store.getAvatarUrl())
                            .goodsName(goods.getName())
                            .mainImg(goods.getImg())
                            .subImg(subImgList)
                            .description(goods.getDescription())
                            .inventory(goods.getInventory())
                            .price(goods.getPrice())
                            .sale(goods.getSales())
                            .createTime(goods.getDate())
                            .build();
    }

    @Override
    public Goods getAvailableGoodsById(Long goodsId) {
        return this.lambdaQuery()
                   .eq(Goods::getId, goodsId)
                   .eq(Goods::getStatus, true)
                   .one();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductInventory(Long goodsId, Integer quantity) {
        Goods goods = this.getById(goodsId);
        if (goods == null || goods.getInventory() < quantity) {
            return false;
        }
        goods.setInventory(goods.getInventory() - quantity);
        return this.updateById(goods);
    }

    @Override
    public List<Goods> getAvailableGoodsByStoreId(Long store) {
        return this.lambdaQuery()
                   .eq(Goods::getStatus, true)
                   .eq(Goods::getStoreId, store)
                   .list();
    }

    @Override
    public IPage<Goods> getGoodsPageAdmin(int page, int size) {
        QueryWrapper<Goods> queryWrapper = new QueryWrapper<>();
        Page<Goods> pageObj = new Page<>(page, size);
        return this.page(pageObj, queryWrapper);
    }

    // ========== 管理员方法 ==========

    @Override
    public IPage<Goods> getStoreGoodsPageAdmin(Long storeId, int page, int size) {
        QueryWrapper<Goods> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("store_id", storeId);
        Page<Goods> pageObj = new Page<>(page, size);
        return this.page(pageObj, queryWrapper);
    }

    @Override
    public IPage<Goods> getGoodsPageMerchant(int page, int size) {
        // 使用原有的 getGoodsPage 逻辑
        return this.getGoodsPage(page, size);
    }

    // ========== 商家方法 ==========

    @Override
    public Goods getGoodsDetailMerchant(Long id) {
        Long userId = UserContextHolder.getUserId();
        Store store = storeService.lambdaQuery()
                                  .eq(Store::getUserId, userId)
                                  .one();
        checkStore(store);

        Goods goods = this.lambdaQuery()
                          .eq(Goods::getId, id)
                          .eq(Goods::getStoreId, store.getId())
                          .one();

        if (goods == null) {
            throw new BusinessException(BizErrorCode.GOODS_NOT_EXIST);
        }

        return goods;
    }

    @Override
    public boolean addGoodsMerchant(Goods goods) {
        Long userId = UserContextHolder.getUserId();
        Store store = storeService.lambdaQuery()
                                  .eq(Store::getUserId, userId)
                                  .one();
        checkStore(store);

        goods.setStoreId(store.getId());
        return this.save(goods);
    }

    @Override
    public boolean updateGoodsMerchant(Goods goods) {
        Long userId = UserContextHolder.getUserId();
        Store store = storeService.lambdaQuery()
                                  .eq(Store::getUserId, userId)
                                  .one();
        checkStore(store);

        Goods existingGoods = this.lambdaQuery()
                                  .eq(Goods::getId, goods.getId())
                                  .eq(Goods::getStoreId, store.getId())
                                  .one();

        if (existingGoods == null) {
            throw new BusinessException(BizErrorCode.GOODS_NOT_EXIST);
        }

        return this.updateById(goods);
    }

    @Override
    public boolean removeGoodsMerchant(Long id) {
        Long userId = UserContextHolder.getUserId();
        Store store = storeService.lambdaQuery()
                                  .eq(Store::getUserId, userId)
                                  .one();
        checkStore(store);

        Goods goods = this.lambdaQuery()
                          .eq(Goods::getId, id)
                          .eq(Goods::getStoreId, store.getId())
                          .one();

        if (goods == null) {
            throw new BusinessException(BizErrorCode.GOODS_NOT_EXIST);
        }

        return this.removeById(id);
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
