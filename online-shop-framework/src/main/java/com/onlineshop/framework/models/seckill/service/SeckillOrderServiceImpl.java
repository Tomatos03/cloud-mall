package com.onlineshop.framework.models.seckill.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.exception.BizException;
import com.onlineshop.framework.models.seckill.entity.SeckillOrder;
import com.onlineshop.framework.models.seckill.enums.SeckillOrderStatusEnum;
import com.onlineshop.framework.models.seckill.mapper.SeckillOrderMapper;
import com.onlineshop.framework.models.seckill.vo.SeckillOrderVO;
import com.onlineshop.framework.utils.AssertUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public SeckillOrderVO participateSeckill(Long seckillActivityId, Long userId, Integer quantity) {
        // 此方法的实现应该在 SeckillManager 中
        // 这里仅提供基础的数据转换逻辑
        throw new BizException(BizErrorCode.SECKILL_FAILED);
    }

    @Override
    public SeckillOrderVO getSeckillOrderDetail(Long seckillOrderId) {
        log.info("查询秒杀订单详情，订单ID: {}", seckillOrderId);

        SeckillOrder order = getById(seckillOrderId);
        AssertUtils.notNull(order, BizErrorCode.SECKILL_ORDER_NOT_EXIST);
        return convertToVO(order);
    }

    @Override
    public SeckillOrderVO getSeckillOrderVO(Long orderId) {
        log.info("查询秒杀订单VO，订单ID: {}", orderId);

        SeckillOrder order = getById(orderId);
        AssertUtils.notNull(order, BizErrorCode.SECKILL_ORDER_NOT_EXIST);
        return convertToVO(order);
    }

    @Override
    public List<SeckillOrderVO> getUserSeckillOrders(Long userId) {
        log.info("查询用户 {} 的秒杀订单列表", userId);

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
    public IPage<SeckillOrderVO> getUserSeckillOrders(Long userId, Integer pageNum, Integer pageSize) {
        log.info("分页查询用户 {} 的秒杀订单，页码: {}, 每页数量: {}", userId, pageNum, pageSize);

        LambdaQueryWrapper<SeckillOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeckillOrder::getUserId, userId)
               .orderByDesc(SeckillOrder::getCreateTime);

        IPage<SeckillOrder> orderPage = this.page(
                new Page<>(pageNum, pageSize), wrapper);

        return convertOrderPage(orderPage);
    }

    @Override
    public boolean confirmSeckillOrder(Long seckillOrderId) {
        log.info("确认秒杀订单，订单ID: {}", seckillOrderId);

        SeckillOrder order = getById(seckillOrderId);
        AssertUtils.notNull(order, BizErrorCode.SECKILL_ORDER_NOT_EXIST);

        AssertUtils.isEqual(order.getStatus(), SeckillOrderStatusEnum.PENDING_PAYMENT.getCode(), 
                BizErrorCode.SECKILL_ORDER_INVALID_STATUS);

        order.setStatus(SeckillOrderStatusEnum.PAID.getCode());
        order.setUpdateTime(LocalDateTime.now());

        return updateById(order);
    }

    @Override
    public boolean cancelSeckillOrder(Long seckillOrderId, String reason) {
        log.info("取消秒杀订单，订单ID: {}, 原因: {}", seckillOrderId, reason);

        SeckillOrder order = getById(seckillOrderId);
        AssertUtils.notNull(order, BizErrorCode.SECKILL_ORDER_NOT_EXIST);

        // 不能取消已取消或已完成的订单
        AssertUtils.isFalse(
                order.getStatus().equals(SeckillOrderStatusEnum.CANCELLED.getCode()) ||
                order.getStatus().equals(SeckillOrderStatusEnum.COMPLETED.getCode()),
                BizErrorCode.SECKILL_ORDER_INVALID_STATUS);

        order.setStatus(SeckillOrderStatusEnum.CANCELLED.getCode());
        order.setCancelReason(reason);
        order.setUpdateTime(LocalDateTime.now());

        return updateById(order);
    }

    @Override
    public boolean cancelSeckillOrderByUser(Long orderId) {
        log.info("用户取消秒杀订单，订单ID: {}", orderId);

        SeckillOrder order = getById(orderId);
        AssertUtils.notNull(order, BizErrorCode.SECKILL_ORDER_NOT_EXIST);

        // 只有待支付状态的订单才能取消
        AssertUtils.isEqual(order.getStatus(), SeckillOrderStatusEnum.PENDING_PAYMENT.getCode(),
                BizErrorCode.SECKILL_ORDER_INVALID_STATUS);

        order.setStatus(SeckillOrderStatusEnum.CANCELLED.getCode());
        order.setUpdateTime(LocalDateTime.now());
        return updateById(order);
    }

    /**
     * 将 SeckillOrder 转换为 SeckillOrderVO，包含关联数据和计算字段
     */
    private SeckillOrderVO convertToVO(SeckillOrder order) {
        SeckillOrderVO vo = new SeckillOrderVO();
        BeanUtils.copyProperties(order, vo);

        // 计算总金额
        if (order.getSeckillPrice() != null && order.getQuantity() != null) {
            vo.setTotalAmount(order.getSeckillPrice()
                                   .multiply(new BigDecimal(order.getQuantity())));
        }

        return vo;
    }

    /**
     * 将订单分页数据转换为VO分页
     */
    private IPage<SeckillOrderVO> convertOrderPage(IPage<SeckillOrder> page) {
        List<SeckillOrderVO> voList = new ArrayList<>();
        for (SeckillOrder order : page.getRecords()) {
            voList.add(convertToVO(order));
        }

        IPage<SeckillOrderVO> voPage = new Page<>();
        voPage.setRecords(voList);
        voPage.setTotal(page.getTotal());
        voPage.setSize(page.getSize());
        voPage.setCurrent(page.getCurrent());

        return voPage;
    }
}