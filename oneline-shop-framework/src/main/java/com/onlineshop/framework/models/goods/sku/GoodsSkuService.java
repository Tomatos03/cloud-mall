package com.onlineshop.framework.models.goods.sku;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class GoodsSkuService extends ServiceImpl<GoodsSkuMapper, GoodsSku> implements IGoodsSkuService {

    @Override
    public List<GoodsSku> listByGoodsId(Long goodsId) {
        return baseMapper.selectList(new QueryWrapper<GoodsSku>()
                                             .eq("goods_id", goodsId));
    }

    @Override
    public GoodsSku getSkuDetail(Long skuId) {
        return baseMapper.selectById(skuId);
    }

    @Override
    public boolean addSku(GoodsSku goodsSku) {
        return save(goodsSku);
    }

    @Override
    public boolean updateSku(GoodsSku goodsSku) {
        return updateById(goodsSku);
    }

    @Override
    public boolean removeSku(Long skuId) {
        return removeById(skuId);
    }

    @Override
    public int removeByGoodsId(Long goodsId) {
        return baseMapper.delete(new QueryWrapper<GoodsSku>()
                                         .eq("goods_id", goodsId));
    }

    @Override
    public boolean deductInventory(Long skuId, Long quantity) {
        GoodsSku sku = baseMapper.selectById(skuId);
        if (sku == null || sku.getInventory() < quantity) {
            return false;
        }
        sku.setInventory(sku.getInventory() - quantity);
        return updateById(sku);
    }

    @Override
    public boolean increaseSales(Long skuId, Long quantity) {
        GoodsSku sku = baseMapper.selectById(skuId);
        if (sku == null) {
            return false;
        }
        sku.setSales(sku.getSales() + quantity);
        return updateById(sku);
    }
}