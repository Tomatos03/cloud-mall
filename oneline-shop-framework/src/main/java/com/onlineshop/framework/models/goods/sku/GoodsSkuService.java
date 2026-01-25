package com.onlineshop.framework.models.goods.sku;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public boolean addSku(GoodsSku goodsSku) {
        return save(goodsSku);
    }

    @Override
    public void removeByGoodsId(Long goodsId) {
        baseMapper.delete(
                new LambdaQueryWrapper<GoodsSku>().eq(GoodsSku::getGoodsId, goodsId)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductInventoryAndIncreaseSales(Long skuId, Integer quantity) {
        GoodsSku sku = baseMapper.selectById(skuId);
        if (sku == null || sku.getInventory() < quantity) {
            log.warn("SKU不存在或库存不足, skuId: {}, inventory: {}, quantity: {}",
                     skuId, sku != null ? sku.getInventory() : null, quantity);
            return;
        }

        // 扣减库存
        sku.setInventory(sku.getInventory() - quantity);
        // 增加销量
        sku.setSales(sku.getSales() + quantity);

        boolean result = updateById(sku);
        if (result) {
            log.info("SKU库存扣减和销量更新成功, skuId: {}, 扣减库存: {}, 增加销量: {}",
                     skuId, quantity, quantity);
        }
    }
}