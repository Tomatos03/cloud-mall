package com.cloudmall.framework.models.order.dto;

import com.cloudmall.framework.common.enums.BizType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/1/25
 */
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class OrderMessage {
    private String orderNo;
    private String orderJson;
    private String topic;
    private String bizType;

    public static OrderMessage of(
            String orderNo,
            String orderJson,
            String topic,
            BizType bizType
    ) {
        return OrderMessage.builder()
                           .orderNo(orderNo)
                           .orderJson(orderJson)
                           .topic(topic)
                           .bizType(bizType.getCode())
                           .build();
    }
}
