package com.onlineshop.framework.models.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineshop.framework.models.seckill.dto.SeckillGoodsDTO;
import com.onlineshop.framework.models.seckill.dto.SeckillGoodsParamsDTO;
import com.onlineshop.framework.models.seckill.entity.SeckillGoods;
import com.onlineshop.framework.models.seckill.mapper.SeckillGoodsMapper;
import com.onlineshop.framework.models.seckill.service.SeckillGoodsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 秒杀商品服务实现
 *
 * @author Tomatos
 * @date 2026/2/26
 */
@Service
@RequiredArgsConstructor
public class SeckillGoodsServiceImpl extends ServiceImpl<SeckillGoodsMapper, SeckillGoods> implements SeckillGoodsService {
    
    @Override
    public IPage<SeckillGoodsDTO> getMyProductsInActivity(Long activityId, Long merchantId, Integer pageNum, Integer pageSize) {
        // 查询当前商家在该活动中的所有秒杀商品
        Page<SeckillGoods> page = new Page<>(pageNum, pageSize);
        IPage<SeckillGoods> result = this.page(page,
                new LambdaQueryWrapper<SeckillGoods>()
                        .eq(SeckillGoods::getActivityId, activityId)
                        .eq(SeckillGoods::getMerchantId, merchantId)
                        .orderByDesc(SeckillGoods::getCreateTime));

        return result.convert(goods -> {
            SeckillGoodsDTO dto = new SeckillGoodsDTO();
            BeanUtils.copyProperties(goods, dto);
            return dto;
        });
    }
    
    @Override
    public IPage<SeckillGoodsDTO> getActivityProducts(SeckillGoodsParamsDTO params) {
        // 查询该活动的所有已通过审核的秒杀商品
        Page<SeckillGoods> page = new Page<>(params.getPage(), params.getPageSize());
        LambdaQueryWrapper<SeckillGoods> wrapper = new LambdaQueryWrapper<SeckillGoods>()
                .eq(SeckillGoods::getActivityId, params.getActivityId());
        
        // 如果指定了商家ID，则进一步筛选
        if (params.getMerchantId() != null) {
            wrapper.eq(SeckillGoods::getMerchantId, params.getMerchantId());
        }
        
        wrapper.orderByDesc(SeckillGoods::getCreateTime);
        
        IPage<SeckillGoods> result = this.page(page, wrapper);

        return result.convert(goods -> {
            SeckillGoodsDTO dto = new SeckillGoodsDTO();
            BeanUtils.copyProperties(goods, dto);
            return dto;
        });
    }
    
    @Override
    public IPage<SeckillGoodsDTO> getActivityProducts(Long activityId, Integer pageNum, Integer pageSize) {
        // 查询该活动的所有已通过审核的秒杀商品
        Page<SeckillGoods> page = new Page<>(pageNum, pageSize);
        IPage<SeckillGoods> result = this.page(page,
                new LambdaQueryWrapper<SeckillGoods>()
                        .eq(SeckillGoods::getActivityId, activityId)
                        .orderByDesc(SeckillGoods::getCreateTime));

        return result.convert(goods -> {
            SeckillGoodsDTO dto = new SeckillGoodsDTO();
            BeanUtils.copyProperties(goods, dto);
            return dto;
        });
    }
    
    @Override
    public SeckillGoodsDTO getSeckillProductDetail(Long id) {
        // 查询秒杀商品详情
        SeckillGoods goods = this.getById(id);
        if (goods == null) {
            return null;
        }
        
        SeckillGoodsDTO dto = new SeckillGoodsDTO();
        BeanUtils.copyProperties(goods, dto);
        return dto;
    }
    
    @Override
    public long countByActivityId(Long activityId) {
        // 统计指定活动中的秒杀商品数量
        return this.count(
                new LambdaQueryWrapper<SeckillGoods>()
                        .eq(SeckillGoods::getActivityId, activityId)
        );
    }
    
    @Override
    public List<SeckillGoods> listByActivityId(Long activityId) {
        // 查询指定活动的所有秒杀商品（不分页）
        return this.list(
                new LambdaQueryWrapper<SeckillGoods>()
                        .eq(SeckillGoods::getActivityId, activityId)
                        .orderByDesc(SeckillGoods::getCreateTime)
        );
    }
}
