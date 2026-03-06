package com.onlineshop.framework.models.seckill.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.onlineshop.framework.models.seckill.dto.SeckillGoodsDTO;
import com.onlineshop.framework.models.seckill.dto.SeckillGoodsParamsDTO;
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
     * 获取商家在指定活动中的秒杀商品（分页）
     * 
     * @param activityId 活动ID
     * @param merchantId 商家ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 秒杀商品分页结果
     */
    IPage<SeckillGoodsDTO> getMyProductsInActivity(Long activityId, Long merchantId, Integer pageNum, Integer pageSize);
    
    /**
     * 获取指定活动的所有秒杀商品（分页）
     * 
     * @param params 秒杀商品查询参数（包含activityId和可选的merchantId）
     * @return 秒杀商品分页结果
     */
    IPage<SeckillGoodsDTO> getActivityProducts(SeckillGoodsParamsDTO params);

    /**
     * 获取指定活动的所有秒杀商品（分页，页码和数量分开传递）
     * 
     * @param activityId 活动ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 秒杀商品分页结果
     */
    IPage<SeckillGoodsDTO> getActivityProducts(Long activityId, Integer pageNum, Integer pageSize);
    
     /**
      * 获取秒杀商品详情
      * 
      * @param id 秒杀商品ID
      * @return 秒杀商品DTO
      */
     SeckillGoodsDTO getSeckillProductDetail(Long id);
     
     /**
      * 统计指定活动中的秒杀商品数量
      * 
      * @param activityId 活动ID
      * @return 秒杀商品数量
      */
     long countByActivityId(Long activityId);
     
     /**
      * 查询指定活动的所有秒杀商品（不分页）
      * 用于批量初始化库存时使用
      * 
      * @param activityId 活动ID
      * @return 秒杀商品列表
      */
     List<SeckillGoods> listByActivityId(Long activityId);
}
