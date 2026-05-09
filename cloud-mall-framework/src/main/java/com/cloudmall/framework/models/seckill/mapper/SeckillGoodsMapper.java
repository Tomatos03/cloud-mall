package com.cloudmall.framework.models.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloudmall.framework.models.seckill.entity.SeckillGoods;
import org.apache.ibatis.annotations.Mapper;

/**
 * 秒杀商品 Mapper
 *
 * @author Tomatos
 * @date 2026/2/26
 */
@Mapper
public interface SeckillGoodsMapper extends BaseMapper<SeckillGoods> {
}
