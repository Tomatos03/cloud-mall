package com.onlineshop.framework.application.seckill.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SeckillParticipateResultVO {
    private Long orderId;
    private String message;
}