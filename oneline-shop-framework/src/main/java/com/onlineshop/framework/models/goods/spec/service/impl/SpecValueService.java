package com.onlineshop.framework.models.goods.spec.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineshop.framework.models.goods.spec.entity.SpecValue;
import com.onlineshop.framework.models.goods.spec.mapper.SpecValueMapper;
import com.onlineshop.framework.models.goods.spec.service.ISpecValueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class SpecValueService extends ServiceImpl<SpecValueMapper, SpecValue> implements ISpecValueService {

    @Override
    public List<SpecValue> listBySpecId(Long specId) {
        return baseMapper.selectList(new QueryWrapper<SpecValue>()
                .eq("spec_id", specId)
                .orderByAsc("sort_order"));
    }

    @Override
    public List<SpecValue> listEnabledBySpecId(Long specId) {
        return baseMapper.selectList(new QueryWrapper<SpecValue>()
                .eq("spec_id", specId)
                .eq("status", 1)
                .orderByAsc("sort_order"));
    }

    @Override
    public SpecValue getValueById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public SpecValue getBySpecIdAndValue(Long specId, String value) {
        return lambdaQuery().eq(SpecValue::getSpecId, specId)
                            .eq(SpecValue::getValue, value)
                            .one();
    }

    @Override
    public boolean addValue(SpecValue specValue) {
        return save(specValue);
    }

    @Override
    public boolean updateValue(SpecValue specValue) {
        return updateById(specValue);
    }

    @Override
    public boolean removeValue(Long id) {
        return removeById(id);
    }

    @Override
    public int removeBySpecId(Long specId) {
        return baseMapper.delete(new QueryWrapper<SpecValue>()
                .eq("spec_id", specId));
    }

    @Override
    public boolean enableValue(Long id) {
        SpecValue value = new SpecValue();
        value.setId(id);
        value.setStatus(1);
        return updateById(value);
    }

    @Override
    public boolean disableValue(Long id) {
        SpecValue value = new SpecValue();
        value.setId(id);
        value.setStatus(0);
        return updateById(value);
    }
}