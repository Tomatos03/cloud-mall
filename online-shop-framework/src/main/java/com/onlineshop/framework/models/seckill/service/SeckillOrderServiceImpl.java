package com.onlineshop.framework.models.seckill.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.exception.BizException;
import com.onlineshop.framework.models.seckill.entity.SeckillOrder;
import com.onlineshop.framework.models.seckill.enums.SeckillOrderStatusEnum;
import com.onlineshop.framework.models.seckill.mapper.SeckillOrderMapper;
import com.onlineshop.framework.models.seckill.vo.SeckillOrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 秒杀订单服务实现
 */
@Slf4j
@Service
public class SeckillOrderServiceImpl extends ServiceImpl<SeckillOrderMapper, SeckillOrder> implements SeckillOrderService {

    @Override
    public SeckillOrderVO participateSeckill(Long seckillActivityId, Long userId, Integer quantity) {
        // 此方法的实现应该在 SeckillManager 中
        // 这里仅提供基础的数据转换逻辑
        throw new BizException(BizErrorCode.SECKILL_FAILED);
    }

    @Override
    public SeckillOrderVO getSeckillOrderDetail(Long seckillOrderId) {
        SeckillOrder order = getById(seckillOrderId);
        if (order == null) {
            throw new BizException(BizErrorCode.SECKILL_ORDER_NOT_EXIST);
        }
        return convertToVO(order);
    }

    @Override
    public List<SeckillOrderVO> getUserSeckillOrders(Long userId) {
        List<SeckillOrder> orders = list(
            new LambdaQueryWrapper<SeckillOrder>()
                .eq(SeckillOrder::getUserId, userId)
                .orderByDesc(SeckillOrder::getCreateTime)
        );
        return orders.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean confirmSeckillOrder(Long seckillOrderId) {
        SeckillOrder order = getById(seckillOrderId);
        if (order == null) {
            throw new BizException(BizErrorCode.SECKILL_ORDER_NOT_EXIST);
        }
        
        if (!order.getStatus().equals(SeckillOrderStatusEnum.PENDING_PAYMENT.getCode())) {
            throw new BizException(BizErrorCode.SECKILL_ORDER_INVALID_STATUS);
        }
        
        order.setStatus(SeckillOrderStatusEnum.PAID.getCode());
        order.setUpdateTime(LocalDateTime.now());
        
        return updateById(order);
    }

    @Override
    public boolean cancelSeckillOrder(Long seckillOrderId, String reason) {
        SeckillOrder order = getById(seckillOrderId);
        if (order == null) {
            throw new BizException(BizErrorCode.SECKILL_ORDER_NOT_EXIST);
        }
        
        if (order.getStatus().equals(SeckillOrderStatusEnum.CANCELLED.getCode()) ||
            order.getStatus().equals(SeckillOrderStatusEnum.COMPLETED.getCode())) {
            throw new BizException(BizErrorCode.SECKILL_ORDER_INVALID_STATUS);
        }
        
        order.setStatus(SeckillOrderStatusEnum.CANCELLED.getCode());
        order.setCancelReason(reason);
        order.setUpdateTime(LocalDateTime.now());
        
        return updateById(order);
    }

    /**
     * 将 SeckillOrder 转换为 SeckillOrderVO
     */
    private SeckillOrderVO convertToVO(SeckillOrder order) {
        SeckillOrderVO vo = new SeckillOrderVO();
        BeanUtils.copyProperties(order, vo);
        return vo;
    }
}