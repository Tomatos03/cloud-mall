package com.cloudmall.framework.models.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.cloudmall.framework.models.seckill.entity.SeckillOrder;
import com.cloudmall.framework.models.seckill.mapper.SeckillOrderMapper;
import com.cloudmall.framework.models.seckill.service.SeckillOrderService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 秒杀订单服务实现
 * 
 * 实现了秒杀订单的各类业务操作，包括：
 * - 订单查询（单个、列表、分页）
 * - 订单状态管理（取消、确认）
 * - VO转换和数据计算
 */
@Slf4j
@Service
public class SeckillOrderServiceImpl extends ServiceImpl<SeckillOrderMapper, SeckillOrder> implements SeckillOrderService {
}