package com.onlineshop.framework.models.seckill.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.onlineshop.framework.models.seckill.entity.SeckillOrder;
import com.onlineshop.framework.models.seckill.dto.SeckillOrderDTO;
import com.onlineshop.framework.models.seckill.vo.SeckillOrderVO;

/**
 * 秒杀订单服务接口
 * 
 * 提供秒杀订单的各类业务操作：
 * - 订单查询（单个、列表）
 * - 订单状态管理（取消、确认等）
 * - 数据转换和计算
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
     * 获取秒杀订单详情（返回VO对象，包含关联数据和计算字段）
     *
     * @param seckillOrderId 秒杀订单ID
     * @return 秒杀订单VO
     */
    SeckillOrderVO getSeckillOrderDetail(Long seckillOrderId);

    /**
     * 获取秒杀订单详情（内部使用，通过订单对象转换）
     *
     * @param orderId 秒杀订单ID
     * @return 秒杀订单VO
     */
    SeckillOrderVO getSeckillOrderVO(Long orderId);

    /**
     * 分页查询用户的秒杀订单列表
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 秒杀订单分页数据
     */
    IPage<SeckillOrderVO> getUserSeckillOrders(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 查询用户的秒杀订单列表（无分页，已弃用，保留向后兼容）
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

    /**
     * 用户端取消秒杀订单（仅限待支付状态）
     *
     * @param orderId 秒杀订单ID
     * @return 是否成功
     */
    boolean cancelSeckillOrderByUser(Long orderId);
}