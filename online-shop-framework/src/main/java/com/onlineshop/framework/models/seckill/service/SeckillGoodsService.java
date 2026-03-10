package com.onlineshop.framework.models.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.onlineshop.framework.models.seckill.dto.SeckillGoodsDTO;
import com.onlineshop.framework.models.seckill.entity.SeckillGoods;

import java.util.List;

/**
 * 秒杀商品服务接口
 *
 * @author Tomatos
 * @date 2026/2/26
 */
public interface SeckillGoodsService extends IService<SeckillGoods> {

    /**
     * 获取秒杀商品详情
     *
     * @param id 秒杀商品ID
     * @return 秒杀商品DTO
     */
    SeckillGoodsDTO getSeckillGoodsDetail(Long id);

            /**
     * 查询指定活动的所有秒杀商品（不分页）
     * 用于批量初始化库存时使用
     *
     * @param activityId 活动ID
     * @return 秒杀商品列表
     */
    List<SeckillGoods> listByActivityId(Long activityId);
}
