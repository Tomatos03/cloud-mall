package com.onlineshop.framework.models.goods.spec.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineshop.framework.models.goods.spec.entity.GoodsSkuSpec;
import com.onlineshop.framework.models.goods.spec.mapper.GoodsSkuSpecMapper;
import com.onlineshop.framework.models.goods.spec.service.IGoodsSkuSpecService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class GoodsSkuSpecService extends ServiceImpl<GoodsSkuSpecMapper, GoodsSkuSpec> implements IGoodsSkuSpecService {

    @Override
    public List<GoodsSkuSpec> listBySkuId(Long skuId) {
        return baseMapper.selectList(new QueryWrapper<GoodsSkuSpec>()
                                             .eq("sku_id", skuId));
    }

    @Override
    public void removeBySkuIds(List<Long> skuIds) {
        baseMapper.delete(new QueryWrapper<GoodsSkuSpec>()
                                  .in("sku_id", skuIds));
    }

    @Override
    public long countBySpecValueId(Long specValueId) {
        return lambdaQuery().eq(GoodsSkuSpec::getSpecValueId, specValueId)
                            .count();
    }
}