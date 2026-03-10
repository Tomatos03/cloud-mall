package com.onlineshop.framework.models.seckill.service.impl;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.models.audit.enums.AuditStatus;
import com.onlineshop.framework.models.seckill.dto.SeckillGoodsDTO;
import com.onlineshop.framework.models.seckill.entity.SeckillGoods;
import com.onlineshop.framework.models.seckill.mapper.SeckillGoodsMapper;
import com.onlineshop.framework.models.seckill.service.SeckillGoodsService;
import com.onlineshop.framework.utils.AssertUtils;

/**
 * 秒杀商品服务实现
 *
 * @author Tomatos
 * @date 2026/2/26
 */
@Service
public class SeckillGoodsServiceImpl extends ServiceImpl<SeckillGoodsMapper, SeckillGoods> implements SeckillGoodsService {

    @Override
    public SeckillGoodsDTO getSeckillGoodsDetail(Long id) {
        // 查询秒杀商品详情
        SeckillGoods goods = this.getById(id);
        AssertUtils.notNull(goods, BizErrorCode.SECKILL_GOODS_NOT_FOUND);

        return SeckillGoodsDTO.builder()
                              .id(goods.getId())
                              .status(AuditStatus.APPROVED.getCode())
                              .skuId(goods.getSkuId())
                              .goodsName(goods.getGoodsName())
                              .mainImageUrl(goods.getMainImageUrl())
                              .seckillPrice(goods.getSeckillPrice())
                              .stock(goods.getStock())
                              .build();
    }

    @Override
    public List<SeckillGoods> listByActivityId(Long activityId) {
        return lambdaQuery().eq(SeckillGoods::getActivityId, activityId)
                            .orderByDesc(SeckillGoods::getCreateTime)
                            .list();
    }
}
