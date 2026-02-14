package com.onlineshop.framework.models.goods.spec.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineshop.framework.models.goods.spec.entity.Spec;
import com.onlineshop.framework.models.goods.spec.mapper.SpecMapper;
import com.onlineshop.framework.models.goods.spec.service.ISpecService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class SpecService extends ServiceImpl<SpecMapper, Spec> implements ISpecService {

    @Override
    public List<Spec> listAllEnabled() {
        return baseMapper.selectList(new QueryWrapper<Spec>()
                                             .eq("status", 1)
                                             .orderByAsc("sort"));
    }

    @Override
    public Spec getSpecById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public Spec getSpecByName(String name) {
        return lambdaQuery().eq(Spec::getName, name)
                            .one();
    }

    @Override
    public boolean addSpec(Spec spec) {
        return save(spec);
    }

    @Override
    public boolean updateSpec(Spec spec) {
        return updateById(spec);
    }

    @Override
    public boolean removeSpec(Long id) {
        return removeById(id);
    }

    @Override
    public boolean enableSpec(Long id) {
        Spec spec = new Spec();
        spec.setId(id);
        spec.setStatus(1);
        return updateById(spec);
    }

    @Override
    public boolean disableSpec(Long id) {
        Spec spec = new Spec();
        spec.setId(id);
        spec.setStatus(0);
        return updateById(spec);
    }
}