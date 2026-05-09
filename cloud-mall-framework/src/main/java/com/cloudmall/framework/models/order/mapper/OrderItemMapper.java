package com.cloudmall.framework.models.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloudmall.framework.models.order.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单明细 Mapper 接口
 *
 * @author : Tomatos
 * @date : 2025/12/24
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
}
