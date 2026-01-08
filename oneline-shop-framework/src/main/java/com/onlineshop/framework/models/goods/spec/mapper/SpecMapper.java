package com.onlineshop.framework.models.goods.spec.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlineshop.framework.models.goods.spec.entity.Spec;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SpecMapper extends BaseMapper<Spec> {
}