package com.cloudmall.framework.models.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;

import com.cloudmall.framework.models.seckill.entity.SeckillOrder;

/**
 * 秒杀订单服务接口
 * 
 * 提供秒杀订单的各类业务操作：
 * - 订单查询（单个、列表）
 * - 订单状态管理（取消、确认等）
 * - 数据转换和计算
 */
public interface SeckillOrderService extends IService<SeckillOrder> {
}
