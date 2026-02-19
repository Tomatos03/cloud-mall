package com.onlineshop.framework.models.seckill.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineshop.framework.models.seckill.entity.SeckillActivity;
import com.onlineshop.framework.models.seckill.mapper.SeckillActivityMapper;
import org.springframework.stereotype.Service;

/**
 * 秒杀活动服务实现
 */
@Service
public class SeckillActivityServiceImpl extends ServiceImpl<SeckillActivityMapper, SeckillActivity> implements SeckillActivityService {
}