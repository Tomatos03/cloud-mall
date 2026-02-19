package com.onlineshop.framework.models.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.onlineshop.framework.models.seckill.entity.SeckillOrder;
import com.onlineshop.framework.models.seckill.dto.SeckillOrderDTO;
import com.onlineshop.framework.models.seckill.vo.SeckillOrderVO;

/**
 * 秒杀订单服务接口
 */
public interface SeckillOrderService extends IService<SeckillOrder> {

    /**
     * 参与秒杀，生成秒杀订单
     *
     * @param seckillActivityId 秒杀活动ID
     * @param userId 用户ID
     * @param quantity 购买数量
     * @return 秒杀订单VO
     */
    SeckillOrderVO participateSeckill(Long seckillActivityId, Long userId, Integer quantity);

    /**
     * 获取秒杀订单详情
     *
     * @param seckillOrderId 秒杀订单ID
     * @return 秒杀订单VO
     */
    SeckillOrderVO getSeckillOrderDetail(Long seckillOrderId);

    /**
     * 查询用户的秒杀订单列表
     *
     * @param userId 用户ID
     * @return 秒杀订单VO列表
     */
    java.util.List<SeckillOrderVO> getUserSeckillOrders(Long userId);

    /**
     * 确认秒杀订单（秒杀成功后确认）
     *
     * @param seckillOrderId 秒杀订单ID
     * @return 是否成功
     */
    boolean confirmSeckillOrder(Long seckillOrderId);

    /**
     * 取消秒杀订单
     *
     * @param seckillOrderId 秒杀订单ID
     * @param reason 取消原因
     * @return 是否成功
     */
    boolean cancelSeckillOrder(Long seckillOrderId, String reason);
}