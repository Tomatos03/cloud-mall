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
    public List<GoodsSkuSpec> listBySpecValueId(Long specValueId) {
        return baseMapper.selectList(new QueryWrapper<GoodsSkuSpec>()
                                             .eq("spec_value_id", specValueId));
    }

    @Override
    public boolean addSpecToSku(Long skuId, Long specId, Long specValueId) {
        GoodsSkuSpec spec = new GoodsSkuSpec();
        spec.setSkuId(skuId);
        spec.setSpecId(specId);
        spec.setSpecValueId(specValueId);
        return save(spec);
    }

    @Override
    public boolean batchAddSpecToSku(List<GoodsSkuSpec> specList) {
        return saveBatch(specList);
    }

    @Override
    public int removeBySkuId(Long skuId) {
        return baseMapper.delete(new QueryWrapper<GoodsSkuSpec>()
                                         .eq("sku_id", skuId));
    }

    @Override
    public int removeBySkuIdAndSpecId(Long skuId, Long specId) {
        return baseMapper.delete(new QueryWrapper<GoodsSkuSpec>()
                                         .eq("sku_id", skuId)
                                         .eq("spec_id", specId));
    }

    @Override
    public int removeBySkuIds(List<Long> skuIds) {
        return baseMapper.delete(new QueryWrapper<GoodsSkuSpec>()
                                         .in("sku_id", skuIds));
    }

    @Override
    public long countBySpecValueId(Long specValueId) {
        return lambdaQuery().eq(GoodsSkuSpec::getSpecValueId, specValueId)
                            .count();
    }
}