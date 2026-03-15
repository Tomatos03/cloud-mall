package com.onlineshop.framework.mq.consumer.goods;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.onlineshop.framework.event.MQTag;
import com.onlineshop.framework.models.search.service.IGoodsEsService;

/**
 * 删除商品 ES 索引消费者
 *
 * @author : Tomatos
 * @date : 2026/3/15
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "rocketmq.name-server")
@RocketMQMessageListener(
        topic = "${mq.topic.goods}",
        selectorExpression = MQTag.GOODS_DELETE_FROM_ES,
        consumerGroup = "${mq.group.goods}"
)
public class GoodsDeleteFromEsConsumer implements RocketMQListener<Long> {
    private final IGoodsEsService goodsEsService;

    @Override
    public void onMessage(Long goodsId) {
        if (goodsId == null) {
            log.warn("删除商品索引消息无效, goodsId: null");
            return;
        }

        goodsEsService.deleteGoodsIndex(goodsId);
        log.info("删除商品索引成功, goodsId: {}", goodsId);
    }
}
