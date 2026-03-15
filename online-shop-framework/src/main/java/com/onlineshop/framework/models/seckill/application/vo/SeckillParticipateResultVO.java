package com.onlineshop.framework.models.seckill.application.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SeckillParticipateResultVO {
    private Long orderId;
    private String message;
}